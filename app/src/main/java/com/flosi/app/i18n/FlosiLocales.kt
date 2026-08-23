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
    val all = listOf(
        FlosiLocale("ar", "العربية", "ar-IQ", true),
        FlosiLocale("en", "English", "en-US"),
        FlosiLocale("zh-CN", "简体中文", "zh-CN"),
        FlosiLocale("es", "Español", "es-ES"),
        FlosiLocale("fr", "Français", "fr-FR"),
        FlosiLocale("de", "Deutsch", "de-DE"),
        FlosiLocale("tr", "Türkçe", "tr-TR"),
        FlosiLocale("fa", "فارسی", "fa-IR", true),
        FlosiLocale("ur", "اردو", "ur-PK", true),
        FlosiLocale("hi", "हिन्दी", "hi-IN"),
        FlosiLocale("pt", "Português", "pt-BR"),
        FlosiLocale("it", "Italiano", "it-IT"),
        FlosiLocale("ru", "Русский", "ru-RU"),
        FlosiLocale("ja", "日本語", "ja-JP"),
        FlosiLocale("ko", "한국어", "ko-KR"),
        FlosiLocale("id", "Bahasa Indonesia", "id-ID"),
        FlosiLocale("ms", "Bahasa Melayu", "ms-MY"),
        FlosiLocale("bn", "বাংলা", "bn-BD"),
        FlosiLocale("nl", "Nederlands", "nl-NL"),
        FlosiLocale("pl", "Polski", "pl-PL"),
        FlosiLocale("sv", "Svenska", "sv-SE"),
        FlosiLocale("th", "ไทย", "th-TH"),
        FlosiLocale("vi", "Tiếng Việt", "vi-VN"),
        FlosiLocale("he", "עברית", "he-IL", true)
    )

    val codes: Set<String> = all.mapTo(linkedSetOf()) { it.code }

    fun get(code: String?): FlosiLocale = all.firstOrNull { it.code == code } ?: all.first()
    fun isSupported(code: String?): Boolean = code in codes
}
