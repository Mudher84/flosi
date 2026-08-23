package com.flosi.app.domain.model

data class DashboardSnapshot(
    val totalBalance: Long = 0,
    val monthIncome: Long = 0,
    val monthExpense: Long = 0,
    val todayIncome: Long = 0,
    val todayExpense: Long = 0,
    val baseCurrency: String = "IQD",
    val unconvertedCurrencies: List<String> = emptyList()
) {
    val hasUnconvertedCurrencies: Boolean get() = unconvertedCurrencies.isNotEmpty()
}

data class CategorySpend(
    val categoryId: Long?,
    val categoryName: String,
    val amount: Long
)

data class SearchHit(
    val type: String,
    val id: Long,
    val title: String,
    val subtitle: String
)
