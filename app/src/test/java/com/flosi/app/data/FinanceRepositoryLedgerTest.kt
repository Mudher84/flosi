package com.flosi.app.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.local.entity.*
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.finance.InvoiceMath
import com.flosi.app.settings.FlosiPreferences
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class FinanceRepositoryLedgerTest {
    private lateinit var context: Context
    private lateinit var db: FlosiDatabase
    private lateinit var prefs: FlosiPreferences
    private lateinit var repo: FinanceRepository

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FlosiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prefs = FlosiPreferences(context)
        prefs.setCurrency("IQD")
        repo = FinanceRepository(db, prefs)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun account(name:String="Main",balance:Long=1_000_000L,currency:String="IQD"):Long =
        repo.addAccount(AccountEntity(name=name,type="bank",currency=currency,openingBalance=balance,currentBalance=balance))

    @Test
    fun incomeExpenseEditAndDeleteKeepBalanceExact() = runBlocking {
        val accountId=account(balance=1_000L)
        val income=repo.addTransaction(TransactionEntity(kind="income",amount=500,title="Salary",accountId=accountId))
        assertEquals(1_500L,db.accountDao().get(accountId)!!.currentBalance)

        val expense=repo.addTransaction(TransactionEntity(kind="expense",amount=200,title="Food",accountId=accountId))
        assertEquals(1_300L,db.accountDao().get(accountId)!!.currentBalance)

        val old=db.transactionDao().get(expense)!!
        repo.editTransaction(old.copy(amount=350,title="Food updated"))
        assertEquals(1_150L,db.accountDao().get(accountId)!!.currentBalance)

        repo.deleteTransaction(income)
        assertEquals(650L,db.accountDao().get(accountId)!!.currentBalance)
        repo.deleteTransaction(expense)
        assertEquals(1_000L,db.accountDao().get(accountId)!!.currentBalance)
    }

    @Test
    fun transferWithFeeAndCurrencyIsReversibleAsOneGroup() = runBlocking {
        val iqd=account("IQD",1_000_000L,"IQD")
        val usd=account("USD",100L,"USD")
        assertTrue(prefs.setExchangeRate("USD","IQD","1500"))

        val (outId,inId)=repo.transfer(iqd,usd,150_000L,5_000L,"test")
        assertEquals(845_000L,db.accountDao().get(iqd)!!.currentBalance)
        assertEquals(200L,db.accountDao().get(usd)!!.currentBalance)
        assertNotNull(db.transactionDao().get(inId))

        repo.deleteTransaction(inId)
        assertEquals(1_000_000L,db.accountDao().get(iqd)!!.currentBalance)
        assertEquals(100L,db.accountDao().get(usd)!!.currentBalance)
        assertTrue(db.transactionDao().get(outId)!!.deleted)
        assertTrue(db.transactionDao().get(inId)!!.deleted)
    }

    @Test
    fun transferCannotOverdrawSource() = runBlocking {
        val a=account("A",100L)
        val b=account("B",0L)
        val error=runCatching{repo.transfer(a,b,100L,1L)}.exceptionOrNull()
        assertNotNull(error)
        assertEquals(100L,db.accountDao().get(a)!!.currentBalance)
        assertEquals(0L,db.accountDao().get(b)!!.currentBalance)
    }

    @Test
    fun categoryMustMatchTransactionKind() = runBlocking {
        val accountId=account()
        val incomeCategory=repo.addCategory(CategoryEntity(name="Salary only",kind="income"))
        val error=runCatching{
            repo.addTransaction(TransactionEntity(kind="expense",amount=10,title="Wrong",accountId=accountId,categoryId=incomeCategory))
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals(1_000_000L,db.accountDao().get(accountId)!!.currentBalance)
    }

    @Test
    fun duplicateCategoryIsRejectedInsteadOfSilentlySucceeding() = runBlocking {
        repo.addCategory(CategoryEntity(name="Food",kind="expense"))
        val error=runCatching{repo.addCategory(CategoryEntity(name="Food",kind="expense"))}.exceptionOrNull()
        assertNotNull(error)
    }

    @Test
    fun goalReservationChangesGoalButNotCashBalanceAndDeleteReversesIt() = runBlocking {
        val accountId=account(balance=1_000L)
        val goalId=repo.addGoal(GoalEntity(title="Trip",targetAmount=800L,accountId=accountId))
        val txId=repo.reserveForGoal(goalId,300L)
        assertEquals(1_000L,db.accountDao().get(accountId)!!.currentBalance)
        assertEquals(300L,db.goalDao().get(goalId)!!.savedAmount)

        repo.deleteTransaction(txId)
        assertEquals(1_000L,db.accountDao().get(accountId)!!.currentBalance)
        assertEquals(0L,db.goalDao().get(goalId)!!.savedAmount)
    }

    @Test
    fun commitmentPaymentDeductsOnceAndMovesRecurringDueDate() = runBlocking {
        val accountId=account(balance=10_000L)
        val due=System.currentTimeMillis()-35L*86_400_000L
        val commitmentId=repo.addCommitment(
            CommitmentEntity(title="Internet",amount=1_000L,accountId=accountId,dueAt=due,repeatRule="monthly")
        )
        val txId=repo.payCommitment(commitmentId)
        assertEquals(9_000L,db.accountDao().get(accountId)!!.currentBalance)
        val updated=db.commitmentDao().get(commitmentId)!!
        assertTrue(updated.active)
        assertNotNull(updated.lastPaidAt)
        assertTrue(updated.dueAt>updated.lastPaidAt!!)

        val deleteError=runCatching{repo.deleteTransaction(txId)}.exceptionOrNull()
        assertNotNull(deleteError)
        assertEquals(9_000L,db.accountDao().get(accountId)!!.currentBalance)
    }

    @Test
    fun paidSaleInvoiceCreditsAccountAndPaymentCannotBeDetached() = runBlocking {
        val accountId=account(balance=0L)
        val lineTotal=InvoiceMath.lineTotal(2.0,500L)
        val invoiceId=repo.createInvoice(
            InvoiceEntity(number="SALE-1",type="sale",currency="IQD",subtotal=lineTotal,total=lineTotal,paidAmount=1_000L),
            listOf(InvoiceItemEntity(invoiceId=0,title="Service",quantity=2.0,unitPrice=500L,lineTotal=lineTotal)),
            accountId
        )
        assertTrue(invoiceId>0L)
        assertEquals(1_000L,db.accountDao().get(accountId)!!.currentBalance)
        val payment=db.transactionDao().search("SALE-1").single()
        assertEquals("invoice_payment",payment.kind)
        assertNotNull(runCatching{repo.deleteTransaction(payment.id)}.exceptionOrNull())
        assertEquals(1_000L,db.accountDao().get(accountId)!!.currentBalance)
    }

    @Test
    fun purchaseInvoiceCannotSpendMoreThanAccountBalance() = runBlocking {
        val accountId=account(balance=500L)
        val lineTotal=InvoiceMath.lineTotal(1.0,1_000L)
        val error=runCatching{
            repo.createInvoice(
                InvoiceEntity(number="BUY-1",type="purchase",currency="IQD",subtotal=lineTotal,total=lineTotal,paidAmount=1_000L),
                listOf(InvoiceItemEntity(invoiceId=0,title="Asset",quantity=1.0,unitPrice=1_000L,lineTotal=lineTotal)),
                accountId
            )
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals(500L,db.accountDao().get(accountId)!!.currentBalance)
    }

    @Test
    fun archivedAccountRejectsNewTransactions() = runBlocking {
        val accountId=repo.addAccount(AccountEntity(name="Old",type="bank",openingBalance=100,currentBalance=100,archived=true))
        val error=runCatching{repo.addTransaction(TransactionEntity(kind="expense",amount=1,title="x",accountId=accountId))}.exceptionOrNull()
        assertNotNull(error)
    }
}
