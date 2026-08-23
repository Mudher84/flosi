package com.flosi.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flosi.app.data.local.dao.TransactionWithNames
import com.flosi.app.data.local.entity.*
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.domain.model.*
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.settings.FlosiPreferencesState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val dashboard: DashboardSnapshot = DashboardSnapshot(),
    val recent: List<TransactionWithNames> = emptyList(),
    val topCategories: List<CategorySpend> = emptyList(),
    val reservedCommitments: Long = 0,
    val reservedGoals: Long = 0,
    val reserveUnconvertedCurrencies: List<String> = emptyList(),
    val loading: Boolean = true
)

data class BudgetProgress(
    val budget: BudgetEntity,
    val spent: Long,
    val remaining: Long,
    val overAmount: Long,
    val usagePercent: Float,
    val missingCurrencies: List<String>
) {
    val isOver: Boolean get() = overAmount > 0L
    val warningReached: Boolean get() = usagePercent >= budget.warningPercent
}

private data class ReserveSnapshot(
    val commitments: Long,
    val goals: Long,
    val missingCurrencies: List<String>
)

class HomeViewModel(private val repo: FinanceRepository): ViewModel() {
    private val reserves: Flow<ReserveSnapshot> = combine(repo.commitments,repo.goals,repo.accounts,repo.preferenceState) { commitments,goals,accounts,prefs ->
        val base=CurrencyConverter.normalizeCode(prefs.currency)
        val accountMap=accounts.associateBy{it.id}
        val missing=linkedSetOf<String>()

        fun convert(amount:Long,currency:String):Long{
            val source=CurrencyConverter.normalizeCode(currency)
            val converted=CurrencyConverter.convert(amount,source,base,prefs.exchangeRates)
            if(converted==null) missing+=source
            return converted?:0L
        }

        val commitmentReserve=commitments.sumOf{item->
            val currency=item.accountId?.let(accountMap::get)?.currency?:base
            convert(item.amount.coerceAtLeast(0L),currency)
        }
        val goalReserve=goals.sumOf{goal->
            val target=goal.targetAmount.coerceAtLeast(0L)
            val saved=goal.savedAmount.coerceAtLeast(0L).coerceAtMost(target)
            val currency=goal.accountId?.let(accountMap::get)?.currency?:base
            convert(saved,currency)
        }

        ReserveSnapshot(
            commitments=commitmentReserve,
            goals=goalReserve,
            missingCurrencies=missing.filter{it!=base}.sorted()
        )
    }

    val state: StateFlow<HomeUiState> = combine(repo.dashboard,repo.transactions,repo.topExpenseCategories,reserves) { dash,tx,cats,reserve ->
        HomeUiState(
            dashboard=dash,
            recent=tx.take(5),
            topCategories=cats,
            reservedCommitments=reserve.commitments,
            reservedGoals=reserve.goals,
            reserveUnconvertedCurrencies=reserve.missingCurrencies,
            loading=false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

class TransactionsViewModel(private val repo: FinanceRepository): ViewModel() {
    private val search = MutableStateFlow("")
    val query = search.asStateFlow()
    val transactions = combine(repo.transactions, search) { list, q ->
        if(q.isBlank()) list else list.filter { listOf(it.title,it.note,it.accountName,it.personName.orEmpty(),it.categoryName.orEmpty()).joinToString(" ").contains(q,ignoreCase=true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun setSearch(v:String){ search.value=v }
    fun delete(id:Long,onDone:(String?)->Unit={})=viewModelScope.launch {
        val error=runCatching{repo.deleteTransaction(id)}.exceptionOrNull()?.message
        onDone(error)
    }
}

class PeopleViewModel(private val repo: FinanceRepository): ViewModel() {
    val people = repo.people.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val preferences=repo.preferenceState.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),FlosiPreferencesState())
    fun add(name:String,phone:String,balance:Long,currency:String,onDone:(String?)->Unit={})=viewModelScope.launch {
        val error=runCatching{
            require(name.isNotBlank()){ "الاسم مطلوب" }
            repo.addPerson(PersonEntity(name=name.trim(),phone=phone.trim(),currency=currency,openingBalance=balance,currentBalance=balance))
        }.exceptionOrNull()?.message
        onDone(error)
    }
}

class AccountsViewModel(private val repo: FinanceRepository): ViewModel() {
    val accounts=repo.accounts.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun add(name:String,type:String,balance:Long,currency:String,onDone:(String?)->Unit={})=viewModelScope.launch {
        val error=runCatching{
            require(name.isNotBlank()){ "اسم الحساب مطلوب" }
            repo.addAccount(AccountEntity(name=name.trim(),type=type,currency=currency,openingBalance=balance,currentBalance=balance))
        }.exceptionOrNull()?.message
        onDone(error)
    }
    fun transfer(from:Long,to:Long,amount:Long,fee:Long=0,note:String="",onDone:(String?)->Unit={})=viewModelScope.launch {
        val error=runCatching{repo.transfer(from,to,amount,fee,note.trim())}.exceptionOrNull()?.message
        onDone(error)
    }
}

class EntryViewModel(private val repo: FinanceRepository): ViewModel() {
    val accounts=repo.accounts.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val people=repo.people.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val categories=repo.categories.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun save(kind:String, amount:Long, title:String, note:String,accountId:Long, personId:Long?, categoryId:Long?, onDone:(String?)->Unit={}) = viewModelScope.launch {
        val error=runCatching{
            require(amount>0){ "المبلغ يجب أن يكون أكبر من صفر" }
            require(title.isNotBlank()){ "البيان مطلوب" }
            repo.addTransaction(TransactionEntity(kind=kind,amount=amount,title=title.trim(),note=note.trim(),accountId=accountId,personId=personId,categoryId=categoryId))
        }.exceptionOrNull()?.message
        onDone(error)
    }
}

class PlanningViewModel(private val repo: FinanceRepository): ViewModel() {
    val commitments=repo.commitments.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val budgets=repo.budgets.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val goals=repo.goals.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val accounts=repo.accounts.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val categories=repo.categories.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val preferences=repo.preferenceState.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),FlosiPreferencesState())
    val topCategories=repo.topExpenseCategories.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())

    val budgetProgress: StateFlow<List<BudgetProgress>> = combine(repo.budgets,repo.transactions,repo.preferenceState) { budgetList,txList,prefs ->
        budgetList.map { budget ->
            val missing=linkedSetOf<String>()
            val budgetCurrency=CurrencyConverter.normalizeCode(budget.currency)
            val spent=txList.asSequence()
                .filter { tx ->
                    tx.kind=="expense" &&
                        tx.occurredAt>=budget.periodStart && tx.occurredAt<=budget.periodEnd &&
                        (budget.categoryId==null || tx.categoryId==budget.categoryId)
                }
                .mapNotNull { tx ->
                    val converted=CurrencyConverter.convert(tx.amount,tx.accountCurrency,budgetCurrency,prefs.exchangeRates)
                    if(converted==null) missing += CurrencyConverter.normalizeCode(tx.accountCurrency)
                    converted
                }
                .sum()
            val remaining=(budget.limitAmount-spent).coerceAtLeast(0L)
            val over=(spent-budget.limitAmount).coerceAtLeast(0L)
            val percent=if(budget.limitAmount>0L) spent.toDouble()*100.0/budget.limitAmount.toDouble() else 0.0
            BudgetProgress(
                budget=budget.copy(currency=budgetCurrency),
                spent=spent,
                remaining=remaining,
                overAmount=over,
                usagePercent=percent.coerceAtMost(Float.MAX_VALUE.toDouble()).toFloat(),
                missingCurrencies=missing.filter{it!=budgetCurrency}.sorted()
            )
        }
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())

    fun addCommitment(item:CommitmentEntity,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.addCommitment(item)}.exceptionOrNull()?.message
        onDone(error)
    }
    fun payCommitment(id:Long,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.payCommitment(id)}.exceptionOrNull()?.message
        onDone(error)
    }
    fun addBudget(item:BudgetEntity,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.addBudget(item)}.exceptionOrNull()?.message
        onDone(error)
    }
    fun addGoal(item:GoalEntity,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.addGoal(item)}.exceptionOrNull()?.message
        onDone(error)
    }
    fun reserveGoal(goalId:Long,amount:Long,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.reserveForGoal(goalId,amount)}.exceptionOrNull()?.message
        onDone(error)
    }
}

class InvoicesViewModel(private val repo: FinanceRepository): ViewModel() {
    val invoices=repo.invoices.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val accounts=repo.accounts.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun create(invoice:InvoiceEntity,items:List<InvoiceItemEntity>,paymentAccountId:Long?,onDone:(Long?,String?)->Unit={_,_->})=viewModelScope.launch{
        val result=runCatching{repo.createInvoice(invoice,items,paymentAccountId)}
        onDone(result.getOrNull(),result.exceptionOrNull()?.message)
    }
}

class CategoriesViewModel(private val repo: FinanceRepository): ViewModel() {
    val categories=repo.categories.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun add(name:String,kind:String,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{
            require(name.isNotBlank()){ "اسم التصنيف مطلوب" }
            repo.addCategory(CategoryEntity(name=name.trim(),kind=kind))
        }.exceptionOrNull()?.message
        onDone(error)
    }
    fun archive(id:Long,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.archiveCategory(id)}.exceptionOrNull()?.message
        onDone(error)
    }
}

class SearchViewModel(private val repo: FinanceRepository): ViewModel() {
    private val _results=MutableStateFlow<List<SearchHit>>(emptyList())
    val results=_results.asStateFlow()
    fun search(q:String)=viewModelScope.launch { _results.value=repo.searchAll(q) }
}

class FlosiVmFactory(private val repo: FinanceRepository): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repo)
        modelClass.isAssignableFrom(TransactionsViewModel::class.java) -> TransactionsViewModel(repo)
        modelClass.isAssignableFrom(PeopleViewModel::class.java) -> PeopleViewModel(repo)
        modelClass.isAssignableFrom(AccountsViewModel::class.java) -> AccountsViewModel(repo)
        modelClass.isAssignableFrom(EntryViewModel::class.java) -> EntryViewModel(repo)
        modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(repo)
        modelClass.isAssignableFrom(PlanningViewModel::class.java) -> PlanningViewModel(repo)
        modelClass.isAssignableFrom(InvoicesViewModel::class.java) -> InvoicesViewModel(repo)
        modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> CategoriesViewModel(repo)
        else -> error("Unknown ViewModel ${modelClass.name}")
    } as T
}
