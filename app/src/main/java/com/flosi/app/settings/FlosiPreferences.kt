package com.flosi.app.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiLocales
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.flosiDataStore by preferencesDataStore(name="flosi_preferences")

data class FlosiPreferencesState(
    val currency: String = "IQD",
    val language: String = "ar",
    val biometricLock: Boolean = false,
    val hideRecents: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val backupEnabled: Boolean = false,
    val exchangeRates: Set<String> = emptySet()
)

class FlosiPreferences(private val context: Context) {
    private object Keys {
        val currency = stringPreferencesKey("currency")
        val language = stringPreferencesKey("language")
        val biometric = booleanPreferencesKey("biometric_lock")
        val hideRecents = booleanPreferencesKey("hide_recents")
        val dailySummary = booleanPreferencesKey("daily_summary")
        val backup = booleanPreferencesKey("backup_enabled")
        val exchangeRates = stringSetPreferencesKey("exchange_rates")
    }

    val state: Flow<FlosiPreferencesState> = context.flosiDataStore.data.map { p ->
        val storedLanguage = p[Keys.language]
        FlosiPreferencesState(
            currency=p[Keys.currency] ?: "IQD",
            language=if (FlosiLocales.isSupported(storedLanguage)) storedLanguage!! else "ar",
            biometricLock=p[Keys.biometric] ?: false,
            hideRecents=p[Keys.hideRecents] ?: false,
            dailySummaryEnabled=p[Keys.dailySummary] ?: true,
            backupEnabled=p[Keys.backup] ?: false,
            exchangeRates=p[Keys.exchangeRates]?.toSet() ?: emptySet()
        )
    }

    suspend fun setCurrency(v:String)=context.flosiDataStore.edit{it[Keys.currency]=CurrencyConverter.normalizeCode(v)}
    suspend fun setLanguage(v:String) {
        require(FlosiLocales.isSupported(v)) { "Unsupported language: $v" }
        context.flosiDataStore.edit { it[Keys.language] = v }
    }
    suspend fun setBiometric(v:Boolean)=context.flosiDataStore.edit{it[Keys.biometric]=v}
    suspend fun setHideRecents(v:Boolean)=context.flosiDataStore.edit{it[Keys.hideRecents]=v}
    suspend fun setDailySummary(v:Boolean)=context.flosiDataStore.edit{it[Keys.dailySummary]=v}
    suspend fun setBackup(v:Boolean)=context.flosiDataStore.edit{it[Keys.backup]=v}

    suspend fun setExchangeRate(from:String,to:String,rawRate:String):Boolean {
        val encoded=CurrencyConverter.encodeRate(from,to,rawRate) ?: return false
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
