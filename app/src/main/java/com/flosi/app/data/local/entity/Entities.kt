package com.flosi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val currency: String = "IQD",
    val openingBalance: Long = 0,
    val currentBalance: Long = 0,
    val colorArgb: Long = 0xFF8B5CF6,
    val iconKey: String = "wallet",
    val includeInTotal: Boolean = true,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "people", indices = [Index("name"), Index("phone")])
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val note: String = "",
    val openingBalance: Long = 0,
    val currentBalance: Long = 0,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,
    val iconKey: String = "circle",
    val colorArgb: Long = 0xFF8B5CF6,
    val sortOrder: Int = 0,
    val system: Boolean = false,
    val archived: Boolean = false
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = PersonEntity::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("accountId"), Index("personId"), Index("categoryId"), Index("goalId"), Index("occurredAt"), Index("kind")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val amount: Long,
    val title: String,
    val note: String = "",
    val accountId: Long,
    val personId: Long? = null,
    val categoryId: Long? = null,
    val goalId: Long? = null,
    val linkedTransactionId: Long? = null,
    val attachmentUri: String? = null,
    val occurredAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

@Entity(tableName = "commitments", indices = [Index("dueAt"), Index("personId"), Index("accountId")])
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val accountId: Long? = null,
    val personId: Long? = null,
    val categoryId: Long? = null,
    val dueAt: Long,
    val repeatRule: String = "none",
    val remindBeforeDays: Int = 3,
    val active: Boolean = true,
    val lastPaidAt: Long? = null
)

@Entity(tableName = "budgets", indices = [Index("categoryId"), Index("periodStart"), Index("periodEnd")])
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val categoryId: Long? = null,
    val limitAmount: Long,
    @ColumnInfo(defaultValue = "'IQD'") val currency: String = "IQD",
    val periodStart: Long,
    val periodEnd: Long,
    val warningPercent: Int = 80,
    val active: Boolean = true
)

@Entity(tableName = "goals", indices = [Index("accountId")])
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val savedAmount: Long = 0,
    val accountId: Long? = null,
    val targetDate: Long? = null,
    val colorArgb: Long = 0xFF8B5CF6,
    val active: Boolean = true
)

@Entity(tableName = "invoices", indices = [Index("personId"), Index("issuedAt"), Index("status")])
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: String = "sale",
    val personId: Long? = null,
    val status: String = "draft",
    @ColumnInfo(defaultValue = "'IQD'") val currency: String = "IQD",
    val subtotal: Long,
    val discount: Long = 0,
    @ColumnInfo(defaultValue = "0") val taxPercent: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val taxAmount: Long = 0,
    val total: Long,
    val paidAmount: Long = 0,
    val note: String = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val dueAt: Long? = null
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [ForeignKey(entity = InvoiceEntity::class, parentColumns = ["id"], childColumns = ["invoiceId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("invoiceId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val title: String,
    val quantity: Double = 1.0,
    val unitPrice: Long,
    val lineTotal: Long
)
