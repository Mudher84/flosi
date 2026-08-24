package com.flosi.app.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiLocales
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale

private val Context.flosiDataStore by preferencesDataStore(name="flosi_preferences")

private fun deviceDefaultCurrency():String = runCatching {
    Currency.getInstance(Locale.getDefault()).currencyCode.uppercase(Locale.ROOT)
}.getOrDefault("USD")

data class FlosiPreferencesState(
    val currency: String = "USD",
    val language: String = "ar",
    val biometricLock: Boolean = false,
    val hideRecents: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val backupEnabled: Boolean = false,
    val exchangeRates: Set<String> = emptySet(),
    val bankSyncEnabled: Boolean = false,
    val salaryAutoAdd: Boolean = false,
    val bankReviewBeforeAdd: Boolean = true
)

class FlosiPreferences(private val context: Context) {
    private object Keys {
        val currency = stringPreferencesKey("currency")
        val language = stringPreferencesKey("language")
        val biometric = booleanPreferencesKey("biometric_lock") // legacy compatibility only
        val hideRecents = booleanPreferencesKey("hide_recents") // legacy compatibility only
        val dailySummary = booleanPreferencesKey("daily_summary")
        val backup = booleanPreferencesKey("backup_enabled")
        val exchangeRates = stringSetPreferencesKey("exchange_rates")
        val bankSync = booleanPreferencesKey("bank_sync_enabled")
        val salaryAuto = booleanPreferencesKey("bank_salary_auto_add")
        val bankReview = booleanPreferencesKey("bank_review_before_add")
    }

    private fun validCurrency(raw:String):String {
        val code=CurrencyConverter.normalizeCode(raw)
        return runCatching { Currency.getInstance(code);code }.getOrDefault(deviceDefaultCurrency())
    }

    val state: Flow<FlosiPreferencesState> = context.flosiDataStore.data.map { p ->
        val rawLanguage = p[Keys.language] ?: "ar"
        val fallbackCurrency=deviceDefaultCurrency()
        FlosiPreferencesState(
            currency=validCurrency(p[Keys.currency] ?: fallbackCurrency),
            language=if(FlosiLocales.isSupported(rawLanguage)) rawLanguage else "ar",
            biometricLock=p[Keys.biometric] ?: false,
            hideRecents=p[Keys.hideRecents] ?: false,
            dailySummaryEnabled=p[Keys.dailySummary] ?: true,
            backupEnabled=p[Keys.backup] ?: false,
            exchangeRates=p[Keys.exchangeRates]?.mapNotNull { entry -> CurrencyConverter.parseRate(entry)?.let { entry } }?.toSet() ?: emptySet(),
            bankSyncEnabled=p[Keys.bankSync] ?: false,
            salaryAutoAdd=p[Keys.salaryAuto] ?: false,
            bankReviewBeforeAdd=p[Keys.bankReview] ?: true
        )
    }

    suspend fun setCurrency(v:String){
        val code=CurrencyConverter.normalizeCode(v)
        require(runCatching{Currency.getInstance(code)}.isSuccess){"Unsupported currency: $code"}
        context.flosiDataStore.edit{it[Keys.currency]=code}
    }
    suspend fun setLanguage(v:String){require(FlosiLocales.isSupported(v)){"Unsupported locale: $v"};context.flosiDataStore.edit{it[Keys.language]=v}}
    suspend fun setBiometric(v:Boolean)=context.flosiDataStore.edit{it[Keys.biometric]=v}
    suspend fun setHideRecents(v:Boolean)=context.flosiDataStore.edit{it[Keys.hideRecents]=v}
    suspend fun setDailySummary(v:Boolean)=context.flosiDataStore.edit{it[Keys.dailySummary]=v}
    suspend fun setBackup(v:Boolean)=context.flosiDataStore.edit{it[Keys.backup]=v}
    suspend fun setBankSyncEnabled(v:Boolean)=context.flosiDataStore.edit{it[Keys.bankSync]=v}
    suspend fun setSalaryAutoAdd(v:Boolean)=context.flosiDataStore.edit{it[Keys.salaryAuto]=v}
    suspend fun setBankReviewBeforeAdd(v:Boolean)=context.flosiDataStore.edit{it[Keys.bankReview]=v}

    suspend fun setExchangeRate(from:String,to:String,rawRate:String):Boolean {
        val f=CurrencyConverter.normalizeCode(from);val t=CurrencyConverter.normalizeCode(to)
        if(runCatching{Currency.getInstance(f);Currency.getInstance(t)}.isFailure)return false
        val encoded=CurrencyConverter.encodeRate(f,t,rawRate) ?: return false
        val parsed=CurrencyConverter.parseRate(encoded) ?: return false
        context.flosiDataStore.edit { prefs ->
            val current=(prefs[Keys.exchangeRates] ?: emptySet()).toMutableSet()
            current.removeAll { entry ->
                val r=CurrencyConverter.parseRate(entry)
                r != null && ((r.from==parsed.from && r.to==parsed.to) || (r.from==parsed.to && r.to==parsed.from))
            }
            current.add(encoded)
            prefs[Keys.exchangeRates]=current
        }
        return true
    }

    suspend fun removeExchangeRate(from:String,to:String) {
        val f=CurrencyConverter.normalizeCode(from)
        val t=CurrencyConverter.normalizeCode(to)
        context.flosiDataStore.edit { prefs ->
            val current=(prefs[Keys.exchangeRates] ?: emptySet()).toMutableSet()
            current.removeAll { entry ->
                val r=CurrencyConverter.parseRate(entry)
                r != null && ((r.from==f && r.to==t) || (r.from==t && r.to==f))
            }
            prefs[Keys.exchangeRates]=current
        }
    }
}
