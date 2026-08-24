package com.flosi.app.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.local.entity.AccountEntity
import com.flosi.app.data.local.entity.PersonEntity
import com.flosi.app.data.local.entity.TransactionEntity
import com.flosi.app.data.repository.FinanceRepository
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
@Config(sdk=[35], application=Application::class)
class PersonCurrencyLedgerTest {
    private lateinit var db:FlosiDatabase
    private lateinit var repo:FinanceRepository

    @Before fun setup(){
        val context=ApplicationProvider.getApplicationContext<Context>()
        db=Room.inMemoryDatabaseBuilder(context,FlosiDatabase::class.java).allowMainThreadQueries().build()
        repo=FinanceRepository(db,FlosiPreferences(context))
    }
    @After fun close(){db.close()}

    @Test fun linkedPersonRequiresSameCurrencyAndReversesExactly()=runBlocking{
        val usd=repo.addAccount(AccountEntity(name="USD",type="bank",currency="USD",openingBalance=1_000,currentBalance=1_000))
        val iqd=repo.addAccount(AccountEntity(name="IQD",type="bank",currency="IQD",openingBalance=1_000_000,currentBalance=1_000_000))
        val person=repo.addPerson(PersonEntity(name="Ali",currency="USD"))

        val id=repo.addTransaction(TransactionEntity(kind="debt_given",amount=100,title="Loan",accountId=usd,personId=person))
        assertEquals(900L,db.accountDao().get(usd)!!.currentBalance)
        assertEquals(100L,db.personDao().get(person)!!.currentBalance)

        repo.deleteTransaction(id)
        assertEquals(1_000L,db.accountDao().get(usd)!!.currentBalance)
        assertEquals(0L,db.personDao().get(person)!!.currentBalance)

        val error=runCatching{repo.addTransaction(TransactionEntity(kind="debt_given",amount=100,title="Wrong currency",accountId=iqd,personId=person))}.exceptionOrNull()
        assertNotNull(error)
        assertEquals(1_000_000L,db.accountDao().get(iqd)!!.currentBalance)
        assertEquals(0L,db.personDao().get(person)!!.currentBalance)
    }
}
