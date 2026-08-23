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

class HomeViewModel(private val repo: FinanceRepository): ViewModel() {
    val state: StateFlow<HomeUiState> = combine(repo.dashboard,repo.transactions,repo.topExpenseCategories,repo.commitments,repo.goals) { dash, tx, cats, commitments, goals ->
        val commitmentReserve = commitments.sumOf { it.amount.coerceAtLeast(0L) }
        val goalReserve = goals.sumOf { goal -> val target = goal.targetAmount.coerceAtLeast(0L); goal.savedAmount.coerceAtLeast(0L).coerceAtMost(target) }
        HomeUiState(dashboard=dash,recent=tx.take(5),topCategories=cats,reservedCommitments=commitmentReserve,reservedGoals=goalReserve,loading=false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

class TransactionsViewModel(private val repo: FinanceRepository): ViewModel() {
    private val search = MutableStateFlow("")
    val query = search.asStateFlow()
    val transactions = combine(repo.transactions, search) { list, q ->
        if(q.isBlank()) list else list.filter { listOf(it.title,it.note,it.accountName,it.personName.orEmpty(),it.categoryName.orEmpty()).joinToString(" ").contains(q,ignoreCase=true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun setSearch(v:String){ search.value=v }
    fun delete(id:Long)=viewModelScope.launch { repo.deleteTransaction(id) }
}

class PeopleViewModel(private val repo: FinanceRepository): ViewModel() {
    val people = repo.people.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun add(name:String,phone:String,balance:Long)=viewModelScope.launch { repo.addPerson(PersonEntity(name=name,phone=phone,openingBalance=balance,currentBalance=balance)) }
}

class AccountsViewModel(private val repo: FinanceRepository): ViewModel() {
    val accounts=repo.accounts.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun add(name:String,type:String,balance:Long,currency:String)=viewModelScope.launch {
        repo.addAccount(AccountEntity(name=name,type=type,currency=currency,openingBalance=balance,currentBalance=balance))
    }
    fun transfer(from:Long,to:Long,amount:Long,fee:Long=0)=viewModelScope.launch { repo.transfer(from,to,amount,fee) }
}

class EntryViewModel(private val repo: FinanceRepository): ViewModel() {
    val accounts=repo.accounts.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val people=repo.people.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    val categories=repo.categories.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun save(kind:String, amount:Long, title:String, note:String,accountId:Long, personId:Long?, categoryId:Long?, onDone:()->Unit={}) = viewModelScope.launch {
        require(amount>0)
        repo.addTransaction(TransactionEntity(kind=kind,amount=amount,title=title,note=note,accountId=accountId,personId=personId,categoryId=categoryId))
        onDone()
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
            val spent=txList.asSequence()
                .filter { tx ->
                    tx.kind=="expense" &&
                        tx.occurredAt>=budget.periodStart && tx.occurredAt<=budget.periodEnd &&
                        (budget.categoryId==null || tx.categoryId==budget.categoryId)
                }
                .mapNotNull { tx ->
                    val converted=CurrencyConverter.convert(tx.amount,tx.accountCurrency,budget.currency,prefs.exchangeRates)
                    if(converted==null) missing += CurrencyConverter.normalizeCode(tx.accountCurrency)
                    converted
                }
                .sum()
            val remaining=(budget.limitAmount-spent).coerceAtLeast(0L)
            val over=(spent-budget.limitAmount).coerceAtLeast(0L)
            val percent=if(budget.limitAmount>0L) spent.toFloat()*100f/budget.limitAmount.toFloat() else 0f
            BudgetProgress(
                budget=budget,
                spent=spent,
                remaining=remaining,
                overAmount=over,
                usagePercent=percent,
                missingCurrencies=missing.filter{it!=budget.currency}.sorted()
            )
        }
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())

    fun addCommitment(item:CommitmentEntity)=viewModelScope.launch{repo.addCommitment(item)}
    fun addBudget(item:BudgetEntity)=viewModelScope.launch{repo.addBudget(item)}
    fun addGoal(item:GoalEntity)=viewModelScope.launch{repo.addGoal(item)}
    fun reserveGoal(goalId:Long,amount:Long,onDone:(String?)->Unit={})=viewModelScope.launch{
        val error=runCatching{repo.reserveForGoal(goalId,amount)}.exceptionOrNull()?.message
        onDone(error)
    }
}

class InvoicesViewModel(private val repo: FinanceRepository): ViewModel() {
    val invoices=repo.invoices.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun create(invoice:InvoiceEntity,items:List<InvoiceItemEntity>,onDone:(Long)->Unit={})=viewModelScope.launch{onDone(repo.createInvoice(invoice,items))}
}

class CategoriesViewModel(private val repo: FinanceRepository): ViewModel() {
    val categories=repo.categories.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),emptyList())
    fun add(name:String,kind:String)=viewModelScope.launch{repo.addCategory(CategoryEntity(name=name,kind=kind))}
    fun archive(id:Long)=viewModelScope.launch{repo.archiveCategory(id)}
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
