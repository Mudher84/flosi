package com.flosi.app.finance

import kotlin.math.abs

enum class InsightSeverity { POSITIVE, INFO, WARNING, CRITICAL }

data class SmartInsight(
    val key: String,
    val severity: InsightSeverity,
    val titles: Map<String, String>,
    val bodies: Map<String, String>
) {
    fun title(language: String) = titles[language] ?: titles.getValue("en")
    fun body(language: String) = bodies[language] ?: bodies.getValue("en")
}

object SmartInsights {
    private fun t(ar:String,en:String,tr:String,fr:String,de:String,es:String)=mapOf(
        "ar" to ar,"en" to en,"tr" to tr,"fr" to fr,"de" to de,"es" to es
    )

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
            t("عندك استحقاقات متأخرة","You have overdue commitments","Gecikmiş yükümlülüklerin var","Vous avez des échéances en retard","Du hast überfällige Verpflichtungen","Tienes compromisos vencidos"),
            t(
                "$overdueCount استحقاق بقيمة ${money(overdueTotal,currency)} يحتاج مراجعة.",
                "$overdueCount commitment(s) worth ${money(overdueTotal,currency)} need attention.",
                "$overdueCount yükümlülük, toplam ${money(overdueTotal,currency)}, kontrol edilmeli.",
                "$overdueCount échéance(s) pour ${money(overdueTotal,currency)} nécessitent votre attention.",
                "$overdueCount Verpflichtung(en) über ${money(overdueTotal,currency)} brauchen Aufmerksamkeit.",
                "$overdueCount compromiso(s) por ${money(overdueTotal,currency)} requieren atención."
            )
        )

        if (upcomingCount > 0 && upcomingTotal > safeToSpend) out += SmartInsight(
            "upcoming_pressure", InsightSeverity.WARNING,
            t("الأسبوع الجاي يحتاج انتباه","Next week needs attention","Gelecek hafta dikkat istiyor","La semaine prochaine demande de l’attention","Nächste Woche braucht Aufmerksamkeit","La próxima semana necesita atención"),
            t(
                "استحقاقاتك القريبة ${money(upcomingTotal,currency)} أعلى من مساحتك الآمنة ${money(safeToSpend,currency)}.",
                "Upcoming dues of ${money(upcomingTotal,currency)} exceed your safe zone of ${money(safeToSpend,currency)}.",
                "Yaklaşan ödemelerin ${money(upcomingTotal,currency)}, güvenli alanın ${money(safeToSpend,currency)} üzerinde.",
                "Vos échéances à venir de ${money(upcomingTotal,currency)} dépassent votre zone sûre de ${money(safeToSpend,currency)}.",
                "Anstehende Zahlungen von ${money(upcomingTotal,currency)} liegen über deiner sicheren Zone von ${money(safeToSpend,currency)}.",
                "Tus próximos pagos de ${money(upcomingTotal,currency)} superan tu zona segura de ${money(safeToSpend,currency)}."
            )
        )

        when {
            ratio == null -> out += SmartInsight(
                "income_missing", InsightSeverity.INFO,
                t("خلّ Flosi يفهم دخلك","Help Flosi understand your income","Flosi gelirini anlasın","Aidez Flosi à comprendre vos revenus","Hilf Flosi, dein Einkommen zu verstehen","Ayuda a Flosi a entender tus ingresos"),
                t("سجّل دخلك حتى نحسب معدل الصرف والتوقع لنهاية الشهر بدقة.","Add your income to unlock spending pace and month-end guidance.","Harcama hızını ve ay sonu tahminini görmek için gelirini ekle.","Ajoutez vos revenus pour obtenir le rythme de dépense et une estimation de fin de mois.","Füge dein Einkommen hinzu, um Ausgabentempo und Monatsendprognose zu sehen.","Añade tus ingresos para ver el ritmo de gasto y la previsión de fin de mes.")
            )
            ratio >= 1.0 -> out += SmartInsight(
                "overspending", InsightSeverity.CRITICAL,
                t("مصروفك تجاوز دخل الشهر","Spending exceeded monthly income","Harcamaların aylık gelirini aştı","Les dépenses ont dépassé les revenus mensuels","Ausgaben haben das Monatseinkommen überschritten","Los gastos superaron los ingresos mensuales"),
                t(
                    "المصروف أعلى من الدخل بـ ${money(abs(expense-income),currency)}. الأفضل تخفف الصرف غير الضروري.",
                    "Spending is ${money(abs(expense-income),currency)} above income. Consider reducing non-essential spending.",
                    "Harcamaların gelirinden ${money(abs(expense-income),currency)} fazla. Zorunlu olmayan giderleri azaltmayı düşün.",
                    "Les dépenses dépassent les revenus de ${money(abs(expense-income),currency)}. Réduisez les dépenses non essentielles.",
                    "Die Ausgaben liegen ${money(abs(expense-income),currency)} über dem Einkommen. Reduziere möglichst unnötige Ausgaben.",
                    "Los gastos superan los ingresos en ${money(abs(expense-income),currency)}. Considera reducir gastos no esenciales."
                )
            )
            ratio >= .80 -> out += SmartInsight(
                "high_spend", InsightSeverity.WARNING,
                t("معدل الصرف مرتفع","Your spending pace is high","Harcama hızın yüksek","Votre rythme de dépense est élevé","Dein Ausgabentempo ist hoch","Tu ritmo de gasto es alto"),
                t(
                    "استخدمت ${(ratio*100).toInt()}% من دخل الشهر، وباقي $daysRemaining يوم.",
                    "You've used ${(ratio*100).toInt()}% of monthly income with $daysRemaining day(s) remaining.",
                    "Aylık gelirinin %${(ratio*100).toInt()}'ini kullandın; $daysRemaining gün kaldı.",
                    "Vous avez utilisé ${(ratio*100).toInt()}% de vos revenus mensuels, avec $daysRemaining jour(s) restant(s).",
                    "Du hast ${(ratio*100).toInt()}% deines Monatseinkommens verbraucht; $daysRemaining Tag(e) bleiben.",
                    "Has usado ${(ratio*100).toInt()}% de tus ingresos mensuales y quedan $daysRemaining día(s)."
                )
            )
            ratio <= .55 -> out += SmartInsight(
                "healthy_pace", InsightSeverity.POSITIVE,
                t("إيقاعك المالي مريح","Your financial pace looks healthy","Finansal tempon sağlıklı görünüyor","Votre rythme financier est sain","Dein finanzielles Tempo sieht gesund aus","Tu ritmo financiero se ve saludable"),
                t(
                    "استخدمت ${(ratio*100).toInt()}% من دخل الشهر. عندك مجال أفضل للادخار والأهداف.",
                    "You've used ${(ratio*100).toInt()}% of monthly income, leaving more room for saving and goals.",
                    "Aylık gelirinin %${(ratio*100).toInt()}'ini kullandın; tasarruf ve hedefler için daha fazla alan var.",
                    "Vous avez utilisé ${(ratio*100).toInt()}% de vos revenus mensuels, ce qui laisse plus de place à l’épargne et aux objectifs.",
                    "Du hast ${(ratio*100).toInt()}% deines Monatseinkommens verbraucht und hast mehr Spielraum für Sparen und Ziele.",
                    "Has usado ${(ratio*100).toInt()}% de tus ingresos mensuales, dejando más margen para ahorrar y cumplir metas."
                )
            )
        }

        if (safeToSpend > 0) out += SmartInsight(
            "daily_allowance", InsightSeverity.INFO,
            t("حد يومي مقترح","Suggested daily allowance","Önerilen günlük limit","Budget quotidien suggéré","Empfohlenes Tagesbudget","Límite diario sugerido"),
            t(
                "حتى تبقى ضمن المساحة الآمنة، حاول تخلي صرفك اليومي قريب من ${money(safeDaily,currency)}.",
                "To stay inside your safe zone, aim for about ${money(safeDaily,currency)} per day.",
                "Güvenli alanda kalmak için günlük harcamanı yaklaşık ${money(safeDaily,currency)} seviyesinde tut.",
                "Pour rester dans votre zone sûre, visez environ ${money(safeDaily,currency)} par jour.",
                "Um in deiner sicheren Zone zu bleiben, peile etwa ${money(safeDaily,currency)} pro Tag an.",
                "Para mantenerte en tu zona segura, intenta gastar unos ${money(safeDaily,currency)} al día."
            )
        )

        return out.distinctBy { it.key }.take(3)
    }

    private fun money(value: Long, currency: String) = "$value $currency"
}
