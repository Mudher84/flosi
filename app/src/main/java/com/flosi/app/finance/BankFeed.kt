package com.flosi.app.finance

import java.util.Locale

data class BankFeedEntry(
    val externalId: String,
    val amount: Long,
    val description: String,
    val occurredAt: Long,
    val currency: String
)

enum class BankFeedKind { SALARY, INCOME, EXPENSE, TRANSFER_IN, TRANSFER_OUT }

object BankFeedClassifier {
    private val salaryWords = listOf(
        "salary", "payroll", "wage", "monthly salary", "راتب", "الراتب", "رواتب", "راتب شهري"
    )
    private val transferWords = listOf(
        "transfer", "internal transfer", "fund transfer", "تحويل", "حوالة", "نقل بين الحسابات"
    )
    private val otherIncomeWords = listOf(
        "pension", "retirement", "allowance", "bonus", "rent income", "تقاعد", "مخصصات", "مكافأة", "ايجار مستلم", "إيجار مستلم"
    )

    fun classify(entry: BankFeedEntry): BankFeedKind {
        require(entry.externalId.isNotBlank()) { "معرف الحركة المصرفية مطلوب" }
        require(entry.amount != 0L) { "الحركة المصرفية لا يمكن أن تكون بصفر" }
        val text = entry.description.trim().lowercase(Locale.ROOT)
        val credit = entry.amount > 0L
        if (transferWords.any(text::contains)) return if (credit) BankFeedKind.TRANSFER_IN else BankFeedKind.TRANSFER_OUT
        if (credit && salaryWords.any(text::contains)) return BankFeedKind.SALARY
        if (credit && otherIncomeWords.any(text::contains)) return BankFeedKind.INCOME
        return if (credit) BankFeedKind.INCOME else BankFeedKind.EXPENSE
    }

    fun title(entry: BankFeedEntry, kind: BankFeedKind = classify(entry)): String = when (kind) {
        BankFeedKind.SALARY -> "راتب"
        BankFeedKind.INCOME -> entry.description.trim().ifBlank { "إيداع مصرفي" }
        BankFeedKind.EXPENSE -> entry.description.trim().ifBlank { "دفع مصرفي" }
        BankFeedKind.TRANSFER_IN -> "تحويل مصرفي وارد"
        BankFeedKind.TRANSFER_OUT -> "تحويل مصرفي صادر"
    }

    fun marker(externalId: String): String {
        val clean = externalId.trim()
        require(clean.isNotEmpty()) { "معرف الحركة المصرفية مطلوب" }
        return "bank-feed:$clean"
    }
}
