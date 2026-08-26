package com.flosi.app.i18n

import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

data class FlosiLocale(
    val code: String,
    val label: String,
    val localeTag: String,
    val rtl: Boolean = false
) {
    val layoutDirection: LayoutDirection get() = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    fun locale(): Locale = Locale.forLanguageTag(localeTag)
}

object FlosiLocales {
    /** Launch locales: intentionally limited so every visible language can be fully QA'd. */
    val all = listOf(
        FlosiLocale("ar", "العربية", "ar-IQ", true),
        FlosiLocale("en", "English", "en-US"),
        FlosiLocale("tr", "Türkçe", "tr-TR"),
        FlosiLocale("fr", "Français", "fr-FR"),
        FlosiLocale("de", "Deutsch", "de-DE"),
        FlosiLocale("es", "Español", "es-ES")
    )

    val codes: Set<String> = all.mapTo(linkedSetOf()) { it.code }

    fun get(code: String?): FlosiLocale = all.firstOrNull { it.code == code } ?: all.first()
    fun isSupported(code: String?): Boolean = code in codes
}
