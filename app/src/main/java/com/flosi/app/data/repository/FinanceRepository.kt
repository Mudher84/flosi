package com.flosi.app.data.repository

import androidx.room.withTransaction
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.local.entity.*
import com.flosi.app.domain.model.CategorySpend
import com.flosi.app.domain.model.DashboardSnapshot
import com.flosi.app.domain.model.SearchHit
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.finance.InvoiceMath
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

    private val editableTransactionKinds=setOf("income","expense","debt_given","debt_received")
    private val internalTransactionKinds=editableTransactionKinds+setOf("transfer_in","transfer_out","invoice_payment","goal_saving")
    private val repeatRules=setOf("none","once","weekly","monthly","yearly","")

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }.timeInMillis

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH,1); set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }.timeInMillis

    private fun exactSum(values:Sequence<Long>):Long=values.fold(0L){acc,value->Math.addExact(acc,value)}
    private fun exactSum(values:Iterable<Long>):Long=values.fold(0L){acc,value->Math.addExact(acc,value)}

    val dashboard: Flow<DashboardSnapshot> = combine(accounts,transactions,preferences.state) { accountList, txList, prefs ->
        val base=CurrencyConverter.normalizeCode(prefs.currency)
        val missing=linkedSetOf<String>()
        fun convert(amount:Long,currency:String):Long? {
            val value=CurrencyConverter.convert(amount,currency,base,prefs.exchangeRates)
            if(value==null) missing += CurrencyConverter.normalizeCode(currency)
            return value
        }
        val total=exactSum(accountList.asSequence().filter{it.includeInTotal}.mapNotNull{convert(it.currentBalance,it.currency)})
        val monthStart=startOfMonth();val todayStart=startOfToday()
        fun sumKinds(from:Long,kinds:Set<String>):Long = exactSum(txList.asSequence().filter{it.occurredAt>=from && it.kind in kinds}.mapNotNull{convert(it.amount,it.accountCurrency)})
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
        val base=CurrencyConverter.normalizeCode(prefs.currency);val monthStart=startOfMonth()
        txList.asSequence()
            .filter{it.kind=="expense" && it.occurredAt>=monthStart}
            .mapNotNull{tx->CurrencyConverter.convert(tx.amount,tx.accountCurrency,base,prefs.exchangeRates)?.let{tx to it}}
            .groupBy({it.first.categoryName ?: "بدون تصنيف"},{it.second})
            .map{(name,values)->CategorySpend(null,name,exactSum(values))}
            .sortedByDescending{it.amount}.take(8)
    }

    suspend fun seedIfEmpty() {
        val existing = accounts.first();if (existing.isNotEmpty()) return
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
            db.personDao().insert(PersonEntity(name="أحمد محمد",phone="07701234567",currency="IQD",openingBalance=125_000,currentBalance=125_000))
            addTransactionInternal(TransactionEntity(kind="expense",amount=25_000,title="وقود السيارة",accountId=cash,personId=null))
        }
    }

    private suspend fun validateTransaction(tx:TransactionEntity,allowInternal:Boolean){
        val allowed=if(allowInternal)internalTransactionKinds else editableTransactionKinds
        require(tx.kind in allowed){"نوع الحركة غير مدعوم"}
        require(tx.amount>0L){"المبلغ يجب أن يكون أكبر من صفر"}
        require(tx.title.trim().isNotEmpty()){ "البيان مطلوب" }
        require(tx.title.length<=240){ "البيان طويل جداً" }
        val account=db.accountDao().get(tx.accountId)?:error("الحساب غير موجود")
        require(!account.archived){"الحساب مؤرشف ولا يقبل حركات جديدة"}
        tx.personId?.let{personId->
            val person=db.personDao().get(personId)?:error("الشخص المرتبط غير موجود")
            require(!person.archived){"الشخص المرتبط مؤرشف"}
            require(CurrencyConverter.normalizeCode(person.currency)==CurrencyConverter.normalizeCode(account.currency)){
                "عملة حساب الشخص ${person.currency} لا تطابق عملة الحساب ${account.currency}"
            }
        }
        tx.categoryId?.let{categoryId->
            val category=db.categoryDao().get(categoryId)?:error("التصنيف غير موجود")
            require(!category.archived){"التصنيف مؤرشف"}
            val accepted=when(tx.kind){
                "expense","debt_given","transfer_out"->category.kind in setOf("expense","both")
                "income","invoice_payment","debt_received","transfer_in"->category.kind in setOf("income","both")
                else->true
            }
            require(accepted){"التصنيف لا يطابق نوع الحركة"}
        }
        if(tx.kind=="goal_saving")require(tx.goalId!=null){"حركة ادخار الهدف تحتاج هدفاً مرتبطاً"}
    }

    private suspend fun adjustAccountChecked(accountId:Long,delta:Long){
        val account=db.accountDao().get(accountId)?:error("الحساب غير موجود")
        Math.addExact(account.currentBalance,delta);db.accountDao().adjustBalance(accountId,delta)
    }

    private suspend fun adjustPersonChecked(personId:Long,delta:Long){
        val person=db.personDao().get(personId)?:error("الشخص غير موجود")
        Math.addExact(person.currentBalance,delta);db.personDao().adjustBalance(personId,delta)
    }

    private suspend fun addTransactionInternal(tx: TransactionEntity): Long {
        validateTransaction(tx,allowInternal=true)
        val clean=tx.copy(title=tx.title.trim(),note=tx.note.trim())
        val id = db.transactionDao().insert(clean);applyAccountingEffect(clean, reverse = false);return id
    }

    suspend fun addTransaction(tx: TransactionEntity): Long = db.withTransaction {
        require(tx.linkedTransactionId==null){"الحركة العادية لا تقبل رابطاً محاسبياً داخلياً"}
        require(tx.goalId==null){"إضافة ادخار الهدف تتم من شاشة الأهداف"}
        validateTransaction(tx,allowInternal=false);addTransactionInternal(tx)
    }

    suspend fun editTransaction(updated: TransactionEntity) = db.withTransaction {
        val old = db.transactionDao().get(updated.id) ?: error("الحركة غير موجودة")
        require(!old.deleted){"الحركة محذوفة"}
        require(old.kind in editableTransactionKinds){"هذه الحركة نظامية ولا تُعدّل من شاشة الحركات"}
        require(old.linkedTransactionId == null) { "هذه الحركة مرتبطة بحركة محاسبية أخرى ولا يمكن تعديلها منفردة" }
        require(updated.kind in editableTransactionKinds) { "نوع الحركة الجديد غير قابل للتعديل يدوياً" }
        require(updated.linkedTransactionId==null&&updated.goalId==null){"لا يمكن ربط حركة يدوية بقيود نظامية"}
        validateTransaction(updated,allowInternal=false)
        applyAccountingEffect(old, reverse = true)
        val clean=updated.copy(title=updated.title.trim(),note=updated.note.trim(),updatedAt = System.currentTimeMillis())
        db.transactionDao().update(clean);applyAccountingEffect(clean, reverse = false)
    }

    suspend fun deleteTransaction(id: Long) = db.withTransaction {
        val selected = db.transactionDao().get(id) ?: return@withTransaction
        if (selected.deleted) return@withTransaction
        require(!selected.note.startsWith("invoice:")){"حركة الفاتورة تُدار من الفاتورة نفسها ولا يمكن حذفها منفردة"}
        require(!selected.note.startsWith("commitment:")){"دفعة الالتزام مرتبطة بموعد الالتزام ولا يمكن حذفها منفردة"}
        val root = when {selected.kind == "transfer_out" -> selected;selected.linkedTransactionId != null -> db.transactionDao().get(selected.linkedTransactionId);else -> null}
        if (root?.kind == "transfer_out") {
            val group = listOf(root) + db.transactionDao().linkedTo(root.id)
            group.filterNot { it.deleted }.forEach { tx ->applyAccountingEffect(tx, reverse = true);db.transactionDao().softDelete(tx.id)}
        } else {applyAccountingEffect(selected, reverse = true);db.transactionDao().softDelete(selected.id)}
    }

    private suspend fun applyAccountingEffect(tx: TransactionEntity, reverse: Boolean) {
        val direction = if (reverse) -1 else 1
        val sign = when (tx.kind) {"income","transfer_in","invoice_payment" -> +1;"expense","transfer_out","debt_given" -> -1;"debt_received" -> +1;else -> 0} * direction
        if (sign != 0) adjustAccountChecked(tx.accountId,Math.multiplyExact(tx.amount,sign.toLong()))
        tx.personId?.let { personId ->
            val personBase = when (tx.kind) {"debt_given" -> tx.amount;"debt_received" -> -tx.amount;"income","invoice_payment" -> -tx.amount;"expense" -> tx.amount;else -> 0}
            val personDelta=Math.multiplyExact(personBase,direction.toLong());if (personDelta != 0L) adjustPersonChecked(personId, personDelta)
        }
        if (tx.kind == "goal_saving") tx.goalId?.let { goalId -> db.goalDao().adjustSaved(goalId, Math.multiplyExact(tx.amount,direction.toLong())) }
    }

    suspend fun transfer(fromAccountId: Long, toAccountId: Long, amount: Long, fee: Long = 0L, note: String = "") = db.withTransaction {
        require(fromAccountId != toAccountId) { "يجب اختيار حسابين مختلفين" };require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" };require(fee >= 0) { "رسوم التحويل لا يمكن أن تكون سالبة" }
        val from=db.accountDao().get(fromAccountId) ?: error("الحساب المصدر غير موجود");val to=db.accountDao().get(toAccountId) ?: error("الحساب المستلم غير موجود")
        require(!from.archived&&!to.archived){"لا يمكن التحويل من أو إلى حساب مؤرشف"}
        val prefs=preferences.state.first();val received=CurrencyConverter.convert(amount,from.currency,to.currency,prefs.exchangeRates) ?: error("لا يوجد سعر تحويل من ${from.currency} إلى ${to.currency}")
        require(received > 0L) { "قيمة التحويل بعد الصرف غير صالحة" };val debit = Math.addExact(amount, fee);require(from.currentBalance>=debit){"رصيد الحساب المصدر لا يكفي للتحويل والرسوم"}
        val now = System.currentTimeMillis();val cleanNote=note.trim()
        val outId = db.transactionDao().insert(TransactionEntity(kind="transfer_out",amount=amount,title="تحويل صادر",note=cleanNote,accountId=fromAccountId,occurredAt=now))
        val inId = db.transactionDao().insert(TransactionEntity(kind="transfer_in",amount=received,title="تحويل وارد",note=cleanNote,accountId=toAccountId,linkedTransactionId=outId,occurredAt=now))
        if (fee > 0L) db.transactionDao().insert(TransactionEntity(kind="expense",amount=fee,title="رسوم تحويل",note=cleanNote,accountId=fromAccountId,linkedTransactionId=outId,occurredAt=now))
        adjustAccountChecked(fromAccountId, -debit);adjustAccountChecked(toAccountId, received);outId to inId
    }

    suspend fun reserveForGoal(goalId: Long, amount: Long): Long = db.withTransaction {
        require(amount > 0L) { "مبلغ الادخار يجب أن يكون أكبر من صفر" }
        val goal = db.goalDao().get(goalId) ?: error("الهدف غير موجود");require(goal.active) { "الهدف غير مفعّل" };require(goal.targetAmount>0L){"قيمة الهدف غير صالحة"}
        val accountId = goal.accountId ?: error("اربط الهدف بحساب قبل إضافة ادخار");val account = db.accountDao().get(accountId) ?: error("الحساب المرتبط بالهدف غير موجود");require(!account.archived){"الحساب المرتبط بالهدف مؤرشف"}
        val saved=goal.savedAmount.coerceIn(0L,goal.targetAmount);val remaining = goal.targetAmount-saved;require(remaining > 0L) { "الهدف مكتمل" };require(amount <= remaining) { "المبلغ أكبر من المتبقي للوصول إلى الهدف" }
        val alreadyReserved = db.goalDao().reservedForAccount(accountId).coerceAtLeast(0L);val availableToReserve = (account.currentBalance - alreadyReserved).coerceAtLeast(0L);require(amount <= availableToReserve) { "المتاح غير المحجوز في الحساب لا يكفي" }
        addTransactionInternal(TransactionEntity(kind = "goal_saving",amount = amount,title = "ادخار: ${goal.title}",note = "goal:$goalId",accountId = accountId,goalId = goal.id))
    }

    private fun nextDueAfter(dueAt: Long, repeatRule: String, paidAt: Long): Long? {
        if (repeatRule in setOf("none","once","")) return null
        require(repeatRule in repeatRules){"قاعدة تكرار الالتزام غير مدعومة"}
        val calendar = Calendar.getInstance().apply { timeInMillis = dueAt };var guard = 0
        do {when (repeatRule) {"weekly" -> calendar.add(Calendar.DAY_OF_YEAR, 7);"monthly" -> calendar.add(Calendar.MONTH, 1);"yearly" -> calendar.add(Calendar.YEAR, 1)};guard++;check(guard<1200 || calendar.timeInMillis>paidAt){"تعذر حساب الموعد التالي للالتزام"}} while (calendar.timeInMillis <= paidAt)
        return calendar.timeInMillis
    }

    suspend fun payCommitment(id: Long): Long = db.withTransaction {
        val item = db.commitmentDao().get(id) ?: error("الالتزام غير موجود");validateCommitment(item);require(item.active) { "الالتزام غير نشط" }
        val accountId = item.accountId ?: error("اربط الالتزام بحساب قبل تسجيل الدفع");val account=db.accountDao().get(accountId) ?: error("الحساب المرتبط بالالتزام غير موجود");require(!account.archived){"الحساب المرتبط بالالتزام مؤرشف"};require(account.currentBalance>=item.amount){"رصيد الحساب لا يكفي لدفع الالتزام"}
        val paidAt = System.currentTimeMillis()
        val txId = addTransactionInternal(TransactionEntity(kind = "expense",amount = item.amount,title = item.title,note = "commitment:${item.id}",accountId = accountId,personId = item.personId,categoryId = item.categoryId,occurredAt = paidAt))
        val nextDue = nextDueAfter(item.dueAt, item.repeatRule, paidAt);db.commitmentDao().update(item.copy(dueAt = nextDue ?: item.dueAt,active = nextDue != null,lastPaidAt = paidAt));txId
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
    suspend fun updateCategory(item:CategoryEntity){validateCategory(item);db.categoryDao().update(item.copy(name=item.name.trim()))}
    suspend fun updateCommitment(item:CommitmentEntity){validateCommitment(item);db.commitmentDao().update(item.copy(title=item.title.trim()))}
    suspend fun updateBudget(item:BudgetEntity){db.budgetDao().update(validateBudget(item))}
    suspend fun updateGoal(item:GoalEntity){val clean=validateGoal(item);db.goalDao().update(clean.copy(savedAmount=clean.savedAmount.coerceIn(0L,clean.targetAmount)))}

    suspend fun updateInvoice(item:InvoiceEntity){
        require(item.number.trim().isNotEmpty()){ "رقم الفاتورة مطلوب" };require(item.type in setOf("sale","purchase")){"نوع الفاتورة غير مدعوم"};require(item.subtotal>=0L&&item.discount>=0L&&item.discount<=item.subtotal){"قيم الفاتورة غير صالحة"};require(item.taxPercent.isFinite()&&item.taxPercent>=0.0){"نسبة الضريبة غير صالحة"};require(item.taxAmount>=0L&&item.total>=0L&&item.paidAmount in 0..item.total){"إجماليات الفاتورة غير صالحة"}
        val taxable=item.subtotal-item.discount;require(Math.addExact(taxable,item.taxAmount)==item.total){"إجمالي الفاتورة غير متطابق"};db.invoiceDao().update(item.copy(number=item.number.trim(),currency=CurrencyConverter.normalizeCode(item.currency)))
    }

    suspend fun addPerson(person: PersonEntity):Long {
        require(person.name.trim().isNotEmpty()){ "اسم الشخص مطلوب" };require(person.name.length<=160){"اسم الشخص طويل جداً"}
        val currency=CurrencyConverter.normalizeCode(person.currency);require(CurrencyConverter.validCode(currency)){"عملة حساب الشخص غير صالحة"}
        return db.personDao().insert(person.copy(name=person.name.trim(),phone=person.phone.trim(),currency=currency,currentBalance=person.openingBalance))
    }

    suspend fun addAccount(account: AccountEntity):Long {
        require(account.name.trim().isNotEmpty()){ "اسم الحساب مطلوب" };require(account.type in setOf("cash","bank","wallet")){"نوع الحساب غير مدعوم"}
        val currency=CurrencyConverter.normalizeCode(account.currency);require(CurrencyConverter.validCode(currency)){"رمز العملة غير صالح"}
        return db.accountDao().insert(account.copy(name=account.name.trim(),currentBalance=account.openingBalance,currency=currency))
    }

    private fun validateCategory(category:CategoryEntity){require(category.name.trim().isNotEmpty()){ "اسم التصنيف مطلوب" };require(category.kind in setOf("expense","income","both")){"نوع التصنيف غير مدعوم"}}
    suspend fun addCategory(category: CategoryEntity):Long {validateCategory(category);val id=db.categoryDao().insert(category.copy(name=category.name.trim()));require(id!=-1L){"يوجد تصنيف بالاسم نفسه"};return id}

    private suspend fun validateCommitment(item:CommitmentEntity){
        require(item.title.trim().isNotEmpty()){ "اسم الالتزام مطلوب" };require(item.amount > 0L) { "قيمة الالتزام يجب أن تكون أكبر من صفر" };require(item.remindBeforeDays >= 0) { "مدة التذكير غير صالحة" };require(item.repeatRule in repeatRules){"قاعدة تكرار الالتزام غير مدعومة"}
        val account=item.accountId?.let { id ->db.accountDao().get(id)?.also{require(!it.archived){"الحساب المرتبط بالالتزام مؤرشف"}}?:error("الحساب المرتبط بالالتزام غير موجود")}
        val person=item.personId?.let { id ->db.personDao().get(id)?.also{require(!it.archived){"الشخص المرتبط بالالتزام مؤرشف"}}?:error("الشخص المرتبط بالالتزام غير موجود")}
        if(account!=null&&person!=null)require(CurrencyConverter.normalizeCode(account.currency)==CurrencyConverter.normalizeCode(person.currency)){"عملة الشخص لا تطابق عملة حساب الالتزام"}
        item.categoryId?.let { id ->val category=db.categoryDao().get(id) ?: error("تصنيف الالتزام غير موجود");require(!category.archived&&category.kind in setOf("expense","both")){"تصنيف الالتزام يجب أن يكون للمصروفات"}}
    }
    suspend fun addCommitment(item: CommitmentEntity): Long {validateCommitment(item);return db.commitmentDao().insert(item.copy(title=item.title.trim()))}

    private suspend fun validateBudget(item:BudgetEntity):BudgetEntity{
        require(item.title.trim().isNotEmpty()){ "اسم الميزانية مطلوب" };require(item.limitAmount > 0L) { "حد الميزانية يجب أن يكون أكبر من صفر" };require(item.periodEnd >= item.periodStart) { "فترة الميزانية غير صالحة" };require(item.warningPercent in 1..100) { "نسبة التنبيه غير صالحة" }
        item.categoryId?.let{id->val category=db.categoryDao().get(id)?:error("تصنيف الميزانية غير موجود");require(!category.archived&&category.kind in setOf("expense","both")){"تصنيف الميزانية يجب أن يكون للمصروفات"}}
        val currency=CurrencyConverter.normalizeCode(item.currency);require(CurrencyConverter.validCode(currency)){"عملة الميزانية غير صالحة"};return item.copy(title=item.title.trim(),currency=currency)
    }
    suspend fun addBudget(item: BudgetEntity): Long = db.budgetDao().insert(validateBudget(item))

    private suspend fun validateGoal(item:GoalEntity):GoalEntity{
        require(item.title.trim().isNotEmpty()){ "اسم الهدف مطلوب" };require(item.targetAmount > 0L) { "قيمة الهدف يجب أن تكون أكبر من صفر" };require(item.accountId != null) { "اختر حساباً مرتبطاً بالهدف" }
        val account=db.accountDao().get(item.accountId) ?: error("الحساب المرتبط بالهدف غير موجود");require(!account.archived){"الحساب المرتبط بالهدف مؤرشف"};require(item.savedAmount>=0L){"المبلغ المدخر لا يمكن أن يكون سالباً"};return item.copy(title=item.title.trim())
    }
    suspend fun addGoal(item: GoalEntity): Long {val clean=validateGoal(item);return db.goalDao().insert(clean.copy(savedAmount=0L))}

    suspend fun createInvoice(invoice: InvoiceEntity,items: List<InvoiceItemEntity>,paymentAccountId: Long? = null): Long = db.withTransaction {
        require(invoice.number.trim().isNotEmpty()){ "رقم الفاتورة مطلوب" };require(invoice.type in setOf("sale","purchase")){"نوع الفاتورة غير مدعوم"}
        invoice.personId?.let{id->val person=db.personDao().get(id)?:error("الشخص المرتبط بالفاتورة غير موجود");require(!person.archived){"الشخص المرتبط بالفاتورة مؤرشف"}}
        require(items.isNotEmpty()) { "أضف بنداً واحداً على الأقل" }
        val normalizedCurrency = CurrencyConverter.normalizeCode(invoice.currency);require(CurrencyConverter.validCode(normalizedCurrency)){"عملة الفاتورة غير صالحة"}
        val canonicalLines = items.map { item ->require(item.title.trim().isNotEmpty()){ "اسم بند الفاتورة مطلوب" };val lineTotal = InvoiceMath.lineTotal(item.quantity, item.unitPrice);require(item.lineTotal == lineTotal) { "إجمالي أحد بنود الفاتورة غير متطابق" };item.copy(title=item.title.trim(),lineTotal = lineTotal)}
        val totals = InvoiceMath.totals(lineTotals = canonicalLines.map { it.lineTotal },discount = invoice.discount,taxPercent = invoice.taxPercent,paid = invoice.paidAmount)
        val canonicalInvoice = invoice.copy(number=invoice.number.trim(),currency = normalizedCurrency,subtotal = totals.subtotal,discount = totals.discount,taxAmount = totals.taxAmount,total = totals.total,paidAmount = totals.paid,status = totals.status)
        val id = db.invoiceDao().insertInvoice(canonicalInvoice);db.invoiceDao().insertItems(canonicalLines.map { it.copy(invoiceId=id) })
        if (totals.paid > 0L) {
            val accountId = paymentAccountId ?: error("اختر حساب استلام الدفعة");val account = db.accountDao().get(accountId) ?: error("حساب استلام الدفعة غير موجود");require(!account.archived){"حساب الدفعة مؤرشف"}
            val prefs = preferences.state.first();val accountAmount = CurrencyConverter.convert(totals.paid, normalizedCurrency, account.currency, prefs.exchangeRates) ?: error("لا يوجد سعر تحويل من $normalizedCurrency إلى ${account.currency}");require(accountAmount > 0L) { "قيمة الدفعة بعد التحويل غير صالحة" }
            if(canonicalInvoice.type=="purchase")require(account.currentBalance>=accountAmount){"رصيد حساب الدفع لا يكفي"}
            val kind = if (canonicalInvoice.type == "purchase") "expense" else "invoice_payment"
            addTransactionInternal(TransactionEntity(kind = kind,amount = accountAmount,title = if (kind == "invoice_payment") "دفعة فاتورة ${canonicalInvoice.number}" else "دفع فاتورة ${canonicalInvoice.number}",note = "invoice:$id",accountId = accountId,occurredAt = canonicalInvoice.issuedAt))
        }
        id
    }

    suspend fun searchAll(query: String): List<SearchHit> {
        val q = query.trim();if (q.isBlank()) return emptyList()
        val tx = db.transactionDao().search(q).map { SearchHit("transaction",it.id,it.title,listOfNotNull(it.categoryName,it.personName,it.accountName).joinToString(" • ")) }
        val peopleHits = db.personDao().search(q).map { SearchHit("person",it.id,it.name,it.phone) }
        val categoryHits = db.categoryDao().search(q).map { SearchHit("category",it.id,it.name,it.kind) }
        return (tx + peopleHits + categoryHits).take(100)
    }
}
