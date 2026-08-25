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
        InvoiceEntity::class, InvoiceItemEntity::class, SalaryEntity::class
    ],
    version = 6,
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
    abstract fun salaryDao(): SalaryDao

    companion object {
        @Volatile private var instance: FlosiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN currency TEXT NOT NULL DEFAULT 'IQD'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN goalId INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_goalId ON transactions(goalId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE invoices ADD COLUMN currency TEXT NOT NULL DEFAULT 'IQD'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE invoices ADD COLUMN taxPercent REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE invoices ADD COLUMN taxAmount INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE people ADD COLUMN currency TEXT NOT NULL DEFAULT 'IQD'")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS salary_profiles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        currency TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        payday INTEGER NOT NULL,
                        nextPayAt INTEGER NOT NULL,
                        active INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_profiles_active ON salary_profiles(active)")
            }
        }

        fun get(context: Context): FlosiDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlosiDatabase::class.java,
                    "flosi.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }

        fun resetInstance() {
            synchronized(this) {
                runCatching { instance?.close() }
                instance = null
            }
        }
    }
}
