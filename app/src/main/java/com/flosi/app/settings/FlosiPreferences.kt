package com.flosi.app.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.flosiDataStore by preferencesDataStore(name="flosi_preferences")

data class FlosiPreferencesState(
    val currency: String = "IQD",
    val language: String = "ar",
    val biometricLock: Boolean = false,
    val hideRecents: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val backupEnabled: Boolean = false
)

class FlosiPreferences(private val context: Context) {
    private object Keys {
        val currency = stringPreferencesKey("currency")
        val language = stringPreferencesKey("language")
        val biometric = booleanPreferencesKey("biometric_lock")
        val hideRecents = booleanPreferencesKey("hide_recents")
        val dailySummary = booleanPreferencesKey("daily_summary")
        val backup = booleanPreferencesKey("backup_enabled")
    }

    val state: Flow<FlosiPreferencesState> = context.flosiDataStore.data.map { p ->
        FlosiPreferencesState(
            currency=p[Keys.currency] ?: "IQD",
            language=p[Keys.language] ?: "ar",
            biometricLock=p[Keys.biometric] ?: false,
            hideRecents=p[Keys.hideRecents] ?: false,
            dailySummaryEnabled=p[Keys.dailySummary] ?: true,
            backupEnabled=p[Keys.backup] ?: false
        )
    }

    suspend fun setCurrency(v:String)=context.flosiDataStore.edit{it[Keys.currency]=v}
    suspend fun setLanguage(v:String)=context.flosiDataStore.edit{it[Keys.language]=v}
    suspend fun setBiometric(v:Boolean)=context.flosiDataStore.edit{it[Keys.biometric]=v}
    suspend fun setHideRecents(v:Boolean)=context.flosiDataStore.edit{it[Keys.hideRecents]=v}
    suspend fun setDailySummary(v:Boolean)=context.flosiDataStore.edit{it[Keys.dailySummary]=v}
    suspend fun setBackup(v:Boolean)=context.flosiDataStore.edit{it[Keys.backup]=v}
}
