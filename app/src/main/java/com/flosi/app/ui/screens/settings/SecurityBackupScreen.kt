package com.flosi.app.ui.screens.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.ActionRow
import com.flosi.app.ui.components.CardBox
import com.flosi.app.ui.components.FlosiPage
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
import kotlinx.coroutines.launch

@Composable
fun SecurityBackupScreen(
    onBack: () -> Unit,
    onBackups: () -> Unit
) {
    val prefs = rememberFlosiPreferences()
    val state by prefs.state.collectAsState(initial = FlosiPreferencesState())
    val scope = rememberCoroutineScope()

    FlosiPage(
        title = "الأمان والنسخ الاحتياطي",
        subtitle = "إعدادات حقيقية",
        onBack = onBack
    ) {
        CardBox {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("قفل بالبصمة", modifier = Modifier.weight(1f))
                Switch(
                    checked = state.biometricLock,
                    onCheckedChange = { enabled ->
                        scope.launch { prefs.setBiometric(enabled) }
                    }
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("إخفاء من Recent Apps", modifier = Modifier.weight(1f))
                Switch(
                    checked = state.hideRecents,
                    onCheckedChange = { enabled ->
                        scope.launch { prefs.setHideRecents(enabled) }
                    }
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("ملخص يومي", modifier = Modifier.weight(1f))
                Switch(
                    checked = state.dailySummaryEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { prefs.setDailySummary(enabled) }
                    }
                )
            }
        }

        CardBox {
            ActionRow(
                title = "إدارة النسخ الاحتياطية",
                subtitle = "Google Drive / محلية",
                onClick = onBackups
            )
        }
    }
}
