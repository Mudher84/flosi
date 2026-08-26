package com.flosi.app.finance

import kotlin.math.abs

enum class InsightSeverity { POSITIVE, INFO, WARNING, CRITICAL }

data class SmartInsight(
    val key: String,
    val severity: InsightSeverity,
    val titleAr: String,
    val bodyAr: String,
    val titleEn: String,
    val bodyEn: String
)

object SmartInsights {
    fun build(
        income: Long,
        expense: Long,
        safeToSpend: Long,
        safeDaily: Long,
        overdueCount: Int,
        overdueTotal: Long,
        upcomingCount: Int,
        upcomingTotal: Long,
        currency: String,
        daysRemaining: Int
    ): List<SmartInsight> {
        val out = mutableListOf<SmartInsight>()
        val ratio = if (income > 0) expense.toDouble() / income.toDouble() else null

        if (overdueCount > 0) out += SmartInsight(
            "overdue", InsightSeverity.CRITICAL,
            "عندك استحقاقات متأخرة",
            "$overdueCount استحقاق بقيمة ${money(overdueTotal, currency)} يحتاج مراجعة.",
            "You have overdue commitments",
            "$overdueCount commitment(s) worth ${money(overdueTotal, currency)} need attention."
        )

        if (upcomingCount > 0 && upcomingTotal > safeToSpend) out += SmartInsight(
            "upcoming_pressure", InsightSeverity.WARNING,
            "الأسبوع الجاي يحتاج انتباه",
            "استحقاقاتك القريبة ${money(upcomingTotal, currency)} أعلى من مساحتك الآمنة ${money(safeToSpend, currency)}.",
            "Next week needs attention",
            "Upcoming dues of ${money(upcomingTotal, currency)} exceed your safe zone of ${money(safeToSpend, currency)}."
        )

        when {
            ratio == null -> out += SmartInsight(
                "income_missing", InsightSeverity.INFO,
                "خلّ Flosi يفهم دخلك",
                "سجّل دخلك حتى نحسب معدل الصرف والتوقع لنهاية الشهر بدقة.",
                "Help Flosi understand your income",
                "Add your income to unlock spending pace and month-end guidance."
            )
            ratio >= 1.0 -> out += SmartInsight(
                "overspending", InsightSeverity.CRITICAL,
                "مصروفك تجاوز دخل الشهر",
                "المصروف أعلى من الدخل بـ ${money(abs(expense - income), currency)}. الأفضل تخفف الصرف غير الضروري.",
                "Spending exceeded monthly income",
                "Spending is ${money(abs(expense - income), currency)} above income. Consider reducing non-essential spending."
            )
            ratio >= .80 -> out += SmartInsight(
                "high_spend", InsightSeverity.WARNING,
                "معدل الصرف مرتفع",
                "استخدمت ${(ratio * 100).toInt()}% من دخل الشهر، وباقي $daysRemaining يوم.",
                "Your spending pace is high",
                "You've used ${(ratio * 100).toInt()}% of monthly income with $daysRemaining day(s) remaining."
            )
            ratio <= .55 -> out += SmartInsight(
                "healthy_pace", InsightSeverity.POSITIVE,
                "إيقاعك المالي مريح",
                "استخدمت ${(ratio * 100).toInt()}% من دخل الشهر. عندك مجال أفضل للادخار والأهداف.",
                "Your financial pace looks healthy",
                "You've used ${(ratio * 100).toInt()}% of monthly income, leaving more room for saving and goals."
            )
        }

        if (safeToSpend > 0) out += SmartInsight(
            "daily_allowance", InsightSeverity.INFO,
            "حد يومي مقترح",
            "حتى تبقى ضمن المساحة الآمنة، حاول تخلي صرفك اليومي قريب من ${money(safeDaily, currency)}.",
            "Suggested daily allowance",
            "To stay inside your safe zone, aim for about ${money(safeDaily, currency)} per day."
        )

        return out.distinctBy { it.key }.take(3)
    }

    private fun money(value: Long, currency: String) = "$value $currency"
}
