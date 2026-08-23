package com.flosi.app.data.repository

import androidx.room.withTransaction
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.local.dao.TransactionWithNames
import com.flosi.app.data.local.entity.*
import com.flosi.app.domain.model.CategorySpend
import com.flosi.app.domain.model.DashboardSnapshot
import com.flosi.app.domain.model.SearchHit
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.settings.FlosiPreferences
import kotlinx.coroutines.flow.*
import java.util.Calendar

class FinanceRepository(
    private val db: FlosiDatabase,
    private val preferences: FlosiPreferences
) {
    val accounts = db.accountDao().observeAll()
    val people = db.personDao().observeAll()
    val categories = db.categoryDao().observeAll()
    val transactions = db.transactionDao().observeAllDetailed()
    val commitments = db.commitmentDao().observeActive()
    val budgets = db.budgetDao().observeActive()
    val goals = db.goalDao().observeActive()
    val invoices = db.invoiceDao().observeAll()
    val preferenceState = preferences.state

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }.timeInMillis

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH,1); set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }.timeInMillis

    val dashboard: Flow<DashboardSnapshot> = combine(accounts,transactions,preferences.state) { accountList, txList, prefs ->
        val base=CurrencyConverter.normalizeCode(prefs.currency)
        val missing=linkedSetOf<String>()
        fun convert(amount:Long,currency:String):Long? {
            val value=CurrencyConverter.convert(amount,currency,base,prefs.exchangeRates)
            if(value==null) missing += CurrencyConverter.normalizeCode(currency)
            return value
        }

        val total=accountList.asSequence()
            .filter{it.includeInTotal}
            .mapNotNull{convert(it.currentBalance,it.currency)}
            .sum()

        val monthStart=startOfMonth()
        val todayStart=startOfToday()
        fun sumKinds(from:Long,kinds:Set<String>):Long = txList.asSequence()
            .filter{it.occurredAt>=from && it.kind in kinds}
            .mapNotNull{convert(it.amount,it.accountCurrency)}
            .sum()

        DashboardSnapshot(
            totalBalance=total,
            monthIncome=sumKinds(monthStart,setOf("income","invoice_payment")),
            monthExpense=sumKinds(monthStart,setOf("expense")),
            todayIncome=sumKinds(todayStart,setOf("income","invoice_payment")),
            todayExpense=sumKinds(todayStart,setOf("expense")),
            baseCurrency=base,
            unconvertedCurrencies=missing.filter{it!=base}.sorted()
        )
    }.distinctUntilChanged()

    val topExpenseCategories: Flow<List<CategorySpend>> = combine(transactions,preferences.state) { txList,prefs ->
        val base=CurrencyConverter.normalizeCode(prefs.currency)
        val monthStart=startOfMonth()
        txList.asSequence()
            .filter{it.kind=="expense" && it.occurredAt>=monthStart}
            .mapNotNull{tx->CurrencyConverter.convert(tx.amount,tx.accountCurrency,base,prefs.exchangeRates)?.let{tx to it}}
            .groupBy({it.first.categoryName ?: "بدون تصنيف"},{it.second})
            .map{(name,values)->CategorySpend(null,name,values.sum())}
            .sortedByDescending{it.amount}
            .take(8)
    }

    suspend fun seedIfEmpty() {
        val existing = accounts.first()
        if (existing.isNotEmpty()) return
        db.withTransaction {
            val cash = db.accountDao().insert(AccountEntity(name="كاش", type="cash", openingBalance=1_250_000, currentBalance=1_250_000))
            db.accountDao().insert(AccountEntity(name="مصرف الرافدين", type="bank", openingBalance=3_100_000, currentBalance=3_100_000))
            db.accountDao().insert(AccountEntity(name="زين كاش", type="wallet", openingBalance=500_000, currentBalance=500_000))
            db.categoryDao().insertAll(listOf(
                CategoryEntity(name="طعام ومطاعم",kind="expense",colorArgb=0xFFFF8B4A,system=true,sortOrder=1),
                CategoryEntity(name="مواصلات",kind="expense",colorArgb=0xFF3FA7F5,system=true,sortOrder=2),
                CategoryEntity(name="فواتير",kind="expense",colorArgb=0xFF8B5CF6,system=true,sortOrder=3),
                CategoryEntity(name="تسوق",kind="expense",colorArgb=0xFF31C68B,system=true,sortOrder=4),
                CategoryEntity(name="راتب",kind="income",colorArgb=0xFF31C68B,system=true,sortOrder=5),
                CategoryEntity(name="ديون",kind="both",colorArgb=0xFFFF6B72,system=true,sortOrder=6)
            ))
            db.personDao().insert(PersonEntity(name="أحمد محمد",phone="07701234567",openingBalance=125_000,currentBalance=125_000))
            addTransactionInternal(TransactionEntity(kind="expense",amount=25_000,title="وقود السيارة",accountId=cash,personId=null))
        }
    }

    private suspend fun addTransactionInternal(tx: TransactionEntity): Long {
        val id = db.transactionDao().insert(tx)
        applyAccountingEffect(tx, reverse = false)
        return id
    }

    suspend fun addTransaction(tx: TransactionEntity): Long = db.withTransaction { addTransactionInternal(tx) }

    suspend fun editTransaction(updated: TransactionEntity) = db.withTransaction {
        val old = db.transactionDao().get(updated.id) ?: return@withTransaction
        applyAccountingEffect(old, reverse = true)
        db.transactionDao().update(updated.copy(updatedAt = System.currentTimeMillis()))
        applyAccountingEffect(updated, reverse = false)
    }

    suspend fun deleteTransaction(id: Long) = db.withTransaction {
        val old = db.transactionDao().get(id) ?: return@withTransaction
        if (!old.deleted) {
            applyAccountingEffect(old, reverse = true)
            db.transactionDao().softDelete(id)
        }
    }

    private suspend fun applyAccountingEffect(tx: TransactionEntity, reverse: Boolean) {
        val direction = if (reverse) -1 else 1
        val sign = when (tx.kind) {
            "income","transfer_in","invoice_payment" -> +1
            "expense","transfer_out","debt_given" -> -1
            "debt_received" -> +1
            else -> 0
        } * direction
        if (sign != 0) db.accountDao().adjustBalance(tx.accountId, tx.amount * sign)

        tx.personId?.let { personId ->
            val personDelta = when (tx.kind) {
                "debt_given" -> tx.amount
                "debt_received" -> -tx.amount
                "income","invoice_payment" -> -tx.amount
                "expense" -> tx.amount
                else -> 0
            } * direction
            if (personDelta != 0L) db.personDao().adjustBalance(personId, personDelta)
        }

        if (tx.kind == "goal_saving") {
            tx.goalId?.let { goalId ->
                db.goalDao().adjustSaved(goalId, tx.amount * direction)
            }
        }
    }

    suspend fun transfer(fromAccountId: Long, toAccountId: Long, amount: Long, fee: Long = 0L, note: String = "") = db.withTransaction {
        require(fromAccountId != toAccountId) { "يجب اختيار حسابين مختلفين" }
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        require(fee >= 0) { "رسوم التحويل لا يمكن أن تكون سالبة" }
        val from=db.accountDao().get(fromAccountId) ?: error("الحساب المصدر غير موجود")
        val to=db.accountDao().get(toAccountId) ?: error("الحساب المستلم غير موجود")
        val prefs=preferences.state.first()
        val received=CurrencyConverter.convert(amount,from.currency,to.currency,prefs.exchangeRates)
            ?: error("لا يوجد سعر تحويل من ${from.currency} إلى ${to.currency}")
        val now = System.currentTimeMillis()
        val outId = db.transactionDao().insert(TransactionEntity(kind="transfer_out",amount=amount,title="تحويل صادر",note=note,accountId=fromAccountId,occurredAt=now))
        val inId = db.transactionDao().insert(TransactionEntity(kind="transfer_in",amount=received,title="تحويل وارد",note=note,accountId=toAccountId,linkedTransactionId=outId,occurredAt=now))
        if (fee > 0L) db.transactionDao().insert(TransactionEntity(kind="expense",amount=fee,title="رسوم تحويل",note=note,accountId=fromAccountId,linkedTransactionId=outId,occurredAt=now))
        db.accountDao().adjustBalance(fromAccountId, -(amount+fee))
        db.accountDao().adjustBalance(toAccountId, received)
        outId to inId
    }

    suspend fun reserveForGoal(goalId: Long, amount: Long): Long = db.withTransaction {
        require(amount > 0L) { "مبلغ الادخار يجب أن يكون أكبر من صفر" }
        val goal = db.goalDao().get(goalId) ?: error("الهدف غير موجود")
        require(goal.active) { "الهدف غير مفعّل" }
        val accountId = goal.accountId ?: error("اربط الهدف بحساب قبل إضافة ادخار")
        val account = db.accountDao().get(accountId) ?: error("الحساب المرتبط بالهدف غير موجود")
        val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0L)
        require(remaining > 0L) { "الهدف مكتمل" }
        require(amount <= remaining) { "المبلغ أكبر من المتبقي للوصول إلى الهدف" }

        val alreadyReserved = db.goalDao().reservedForAccount(accountId)
        val availableToReserve = (account.currentBalance - alreadyReserved).coerceAtLeast(0L)
        require(amount <= availableToReserve) { "المتاح غير المحجوز في الحساب لا يكفي" }

        addTransactionInternal(
            TransactionEntity(
                kind = "goal_saving",
                amount = amount,
                title = "ادخار: ${goal.title}",
                note = "حجز للهدف بدون اعتباره مصروفاً",
                accountId = accountId,
                goalId = goal.id
            )
        )
    }

    fun observeAccount(id:Long) = db.accountDao().observe(id)
    fun observePerson(id:Long) = db.personDao().observe(id)
    fun observePersonTransactions(id:Long) = db.transactionDao().observeForPerson(id)
    fun observeTransaction(id:Long) = db.transactionDao().observeDetailed(id)
    fun observeCommitment(id:Long) = db.commitmentDao().observe(id)
    fun observeBudget(id:Long) = db.budgetDao().observe(id)
    fun observeGoal(id:Long) = db.goalDao().observe(id)
    fun observeInvoice(id:Long) = db.invoiceDao().observe(id)
    fun observeInvoiceItems(id:Long) = db.invoiceDao().observeItems(id)
    suspend fun archiveCategory(id:Long) = db.categoryDao().archive(id)
    suspend fun updateCategory(item:CategoryEntity) = db.categoryDao().update(item)
    suspend fun updateCommitment(item:CommitmentEntity) = db.commitmentDao().update(item)
    suspend fun updateBudget(item:BudgetEntity) = db.budgetDao().update(item)
    suspend fun updateGoal(item:GoalEntity) = db.goalDao().update(item.copy(savedAmount=item.savedAmount.coerceIn(0L,item.targetAmount.coerceAtLeast(0L))))
    suspend fun updateInvoice(item:InvoiceEntity) = db.invoiceDao().update(item)
    suspend fun addPerson(person: PersonEntity) = db.personDao().insert(person.copy(currentBalance=person.openingBalance))
    suspend fun addAccount(account: AccountEntity) = db.accountDao().insert(account.copy(currentBalance=account.openingBalance))
    suspend fun addCategory(category: CategoryEntity) = db.categoryDao().insert(category)
    suspend fun addCommitment(item: CommitmentEntity) = db.commitmentDao().insert(item)

    suspend fun addBudget(item: BudgetEntity): Long {
        require(item.limitAmount > 0L) { "حد الميزانية يجب أن يكون أكبر من صفر" }
        require(item.periodEnd >= item.periodStart) { "فترة الميزانية غير صالحة" }
        require(item.warningPercent in 1..100) { "نسبة التنبيه غير صالحة" }
        return db.budgetDao().insert(item.copy(currency=CurrencyConverter.normalizeCode(item.currency)))
    }

    suspend fun addGoal(item: GoalEntity): Long {
        require(item.targetAmount > 0L) { "قيمة الهدف يجب أن تكون أكبر من صفر" }
        require(item.accountId != null) { "اختر حساباً مرتبطاً بالهدف" }
        return db.goalDao().insert(item.copy(savedAmount=0L))
    }

    suspend fun createInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long = db.withTransaction {
        val id = db.invoiceDao().insertInvoice(invoice)
        db.invoiceDao().insertItems(items.map { it.copy(invoiceId=id) })
        id
    }

    suspend fun searchAll(query: String): List<SearchHit> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        val tx = db.transactionDao().search(q).map { SearchHit("transaction",it.id,it.title,listOfNotNull(it.categoryName,it.personName,it.accountName).joinToString(" • ")) }
        val peopleHits = db.personDao().search(q).map { SearchHit("person",it.id,it.name,it.phone) }
        val categoryHits = db.categoryDao().search(q).map { SearchHit("category",it.id,it.name,it.kind) }
        return (tx + peopleHits + categoryHits).take(100)
    }
}
