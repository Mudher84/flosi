package com.flosi.app.data.local.dao

import androidx.room.*
import com.flosi.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY createdAt")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id=:id LIMIT 1")
    suspend fun get(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id=:id LIMIT 1")
    fun observe(id: Long): Flow<AccountEntity?>

    @Query("SELECT COALESCE(SUM(currentBalance),0) FROM accounts WHERE includeInTotal=1 AND archived=0")
    fun observeTotalBalance(): Flow<Long>

    @Insert suspend fun insert(entity: AccountEntity): Long
    @Update suspend fun update(entity: AccountEntity)

    @Query("UPDATE accounts SET currentBalance = currentBalance + :delta WHERE id=:id")
    suspend fun adjustBalance(id: Long, delta: Long)
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE archived=0 ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PersonEntity>>
    @Query("SELECT * FROM people WHERE id=:id LIMIT 1") suspend fun get(id: Long): PersonEntity?
    @Query("SELECT * FROM people WHERE id=:id LIMIT 1") fun observe(id: Long): Flow<PersonEntity?>
    @Insert suspend fun insert(entity: PersonEntity): Long
    @Update suspend fun update(entity: PersonEntity)
    @Query("UPDATE people SET currentBalance = currentBalance + :delta WHERE id=:id") suspend fun adjustBalance(id: Long, delta: Long)
    @Query("SELECT * FROM people WHERE archived=0 AND (name LIKE '%' || :q || '%' OR phone LIKE '%' || :q || '%') ORDER BY name LIMIT 40") suspend fun search(q: String): List<PersonEntity>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE archived=0 ORDER BY sortOrder,name") fun observeAll(): Flow<List<CategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CategoryEntity): Long
    @Query("SELECT * FROM categories WHERE id=:id LIMIT 1") suspend fun get(id: Long): CategoryEntity?
    @Query("UPDATE categories SET archived=1 WHERE id=:id") suspend fun archive(id: Long)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(items: List<CategoryEntity>)
    @Update suspend fun update(entity: CategoryEntity)
    @Query("SELECT * FROM categories WHERE archived=0 AND name LIKE '%' || :q || '%' LIMIT 40") suspend fun search(q: String): List<CategoryEntity>
}

data class TransactionWithNames(
    val id: Long,
    val kind: String,
    val amount: Long,
    val title: String,
    val note: String,
    val occurredAt: Long,
    val accountName: String,
    val accountCurrency: String,
    val personName: String?,
    val categoryId: Long?,
    val categoryName: String?
)

data class CategorySpendRow(val categoryId: Long?, val categoryName: String?, val amount: Long)

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id,t.kind,t.amount,t.title,t.note,t.occurredAt,
               a.name AS accountName,a.currency AS accountCurrency,p.name AS personName,
               t.categoryId AS categoryId,c.name AS categoryName
        FROM transactions t
        JOIN accounts a ON a.id=t.accountId
        LEFT JOIN people p ON p.id=t.personId
        LEFT JOIN categories c ON c.id=t.categoryId
        WHERE t.deleted=0
        ORDER BY t.occurredAt DESC
    """)
    fun observeAllDetailed(): Flow<List<TransactionWithNames>>

    @Query("""
        SELECT t.id,t.kind,t.amount,t.title,t.note,t.occurredAt,
               a.name AS accountName,a.currency AS accountCurrency,p.name AS personName,
               t.categoryId AS categoryId,c.name AS categoryName
        FROM transactions t
        JOIN accounts a ON a.id=t.accountId
        LEFT JOIN people p ON p.id=t.personId
        LEFT JOIN categories c ON c.id=t.categoryId
        WHERE t.deleted=0 AND t.personId=:personId
        ORDER BY t.occurredAt DESC
    """)
    fun observeForPerson(personId: Long): Flow<List<TransactionWithNames>>

    @Query("SELECT * FROM transactions WHERE id=:id LIMIT 1") suspend fun get(id: Long): TransactionEntity?
    @Query("SELECT * FROM transactions WHERE linkedTransactionId=:rootId AND deleted=0 ORDER BY id") suspend fun linkedTo(rootId: Long): List<TransactionEntity>

    @Query("""
        SELECT t.id,t.kind,t.amount,t.title,t.note,t.occurredAt,
               a.name AS accountName,a.currency AS accountCurrency,p.name AS personName,
               t.categoryId AS categoryId,c.name AS categoryName
        FROM transactions t
        JOIN accounts a ON a.id=t.accountId
        LEFT JOIN people p ON p.id=t.personId
        LEFT JOIN categories c ON c.id=t.categoryId
        WHERE t.id=:id AND t.deleted=0 LIMIT 1
    """)
    fun observeDetailed(id: Long): Flow<TransactionWithNames?>

    @Insert suspend fun insert(entity: TransactionEntity): Long
    @Update suspend fun update(entity: TransactionEntity)
    @Query("UPDATE transactions SET deleted=1, updatedAt=:now WHERE id=:id") suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    @Query("""SELECT COALESCE(SUM(CASE WHEN kind IN ('income','invoice_payment') THEN amount ELSE 0 END),0)
        FROM transactions WHERE deleted=0 AND occurredAt BETWEEN :from AND :to""")
    fun observeIncome(from: Long, to: Long): Flow<Long>

    @Query("""SELECT COALESCE(SUM(CASE WHEN kind='expense' THEN amount ELSE 0 END),0)
        FROM transactions WHERE deleted=0 AND occurredAt BETWEEN :from AND :to""")
    fun observeExpense(from: Long, to: Long): Flow<Long>

    @Query("""
        SELECT t.categoryId AS categoryId,c.name AS categoryName,COALESCE(SUM(t.amount),0) AS amount
        FROM transactions t LEFT JOIN categories c ON c.id=t.categoryId
        WHERE t.deleted=0 AND t.kind='expense' AND t.occurredAt BETWEEN :from AND :to
        GROUP BY t.categoryId,c.name ORDER BY amount DESC LIMIT :limit
    """)
    fun observeTopExpenseCategories(from: Long, to: Long, limit: Int = 8): Flow<List<CategorySpendRow>>

    @Query("""
        SELECT t.id,t.kind,t.amount,t.title,t.note,t.occurredAt,
               a.name AS accountName,a.currency AS accountCurrency,p.name AS personName,
               t.categoryId AS categoryId,c.name AS categoryName
        FROM transactions t
        JOIN accounts a ON a.id=t.accountId
        LEFT JOIN people p ON p.id=t.personId
        LEFT JOIN categories c ON c.id=t.categoryId
        WHERE t.deleted=0 AND (
            t.title LIKE '%' || :q || '%' OR t.note LIKE '%' || :q || '%' OR
            a.name LIKE '%' || :q || '%' OR p.name LIKE '%' || :q || '%' OR c.name LIKE '%' || :q || '%'
        ) ORDER BY t.occurredAt DESC LIMIT 80
    """)
    suspend fun search(q: String): List<TransactionWithNames>
}

@Dao interface CommitmentDao {
    @Query("SELECT * FROM commitments WHERE active=1 ORDER BY dueAt") fun observeActive(): Flow<List<CommitmentEntity>>
    @Query("SELECT * FROM commitments WHERE id=:id LIMIT 1") fun observe(id:Long): Flow<CommitmentEntity?>
    @Query("SELECT * FROM commitments WHERE id=:id LIMIT 1") suspend fun get(id:Long): CommitmentEntity?
    @Insert suspend fun insert(entity: CommitmentEntity): Long
    @Update suspend fun update(entity: CommitmentEntity)
}

@Dao interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE active=1 ORDER BY periodEnd") fun observeActive(): Flow<List<BudgetEntity>>
    @Query("SELECT * FROM budgets WHERE id=:id LIMIT 1") fun observe(id:Long): Flow<BudgetEntity?>
    @Insert suspend fun insert(entity: BudgetEntity): Long
    @Update suspend fun update(entity: BudgetEntity)
}

@Dao interface GoalDao {
    @Query("SELECT * FROM goals WHERE active=1 ORDER BY targetDate") fun observeActive(): Flow<List<GoalEntity>>
    @Query("SELECT * FROM goals WHERE id=:id LIMIT 1") fun observe(id:Long): Flow<GoalEntity?>
    @Query("SELECT * FROM goals WHERE id=:id LIMIT 1") suspend fun get(id:Long): GoalEntity?
    @Query("SELECT COALESCE(SUM(savedAmount),0) FROM goals WHERE active=1 AND accountId=:accountId") suspend fun reservedForAccount(accountId:Long): Long
    @Query("UPDATE goals SET savedAmount = MIN(targetAmount, MAX(0, savedAmount + :delta)) WHERE id=:id") suspend fun adjustSaved(id:Long,delta:Long)
    @Insert suspend fun insert(entity: GoalEntity): Long
    @Update suspend fun update(entity: GoalEntity)
}

@Dao interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issuedAt DESC") fun observeAll(): Flow<List<InvoiceEntity>>
    @Query("SELECT * FROM invoices WHERE id=:id LIMIT 1") fun observe(id:Long): Flow<InvoiceEntity?>
    @Query("SELECT * FROM invoice_items WHERE invoiceId=:invoiceId ORDER BY id") fun observeItems(invoiceId: Long): Flow<List<InvoiceItemEntity>>
    @Insert suspend fun insertInvoice(entity: InvoiceEntity): Long
    @Insert suspend fun insertItems(items: List<InvoiceItemEntity>)
    @Update suspend fun update(entity: InvoiceEntity)
}
