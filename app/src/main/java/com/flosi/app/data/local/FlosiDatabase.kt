package com.flosi.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flosi.app.data.local.dao.*
import com.flosi.app.data.local.entity.*

@Database(
    entities = [
        AccountEntity::class, PersonEntity::class, CategoryEntity::class, TransactionEntity::class,
        CommitmentEntity::class, BudgetEntity::class, GoalEntity::class,
        InvoiceEntity::class, InvoiceItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class FlosiDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun personDao(): PersonDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun commitmentDao(): CommitmentDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile private var instance: FlosiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN currency TEXT NOT NULL DEFAULT 'IQD'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN goalId INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_goalId ON transactions(goalId)")
            }
        }

        fun get(context: Context): FlosiDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlosiDatabase::class.java,
                    "flosi.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
    }
}
