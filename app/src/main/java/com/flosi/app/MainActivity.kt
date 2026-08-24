package com.flosi.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.fragment.app.FragmentActivity
import com.flosi.app.auth.FlosiAuthGate
import com.flosi.app.i18n.FlosiLocales
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.navigation.FlosiApp
import com.flosi.app.ui.theme.FlosiTheme
import java.util.Locale

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = application as FlosiApplication
            val prefs by app.preferences.state.collectAsState(initial = FlosiPreferencesState())
            val locale = remember(prefs.language) { FlosiLocales.get(prefs.language) }

            LaunchedEffect(locale.code) {
                Locale.setDefault(locale.locale())
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides locale.layoutDirection,
                LocalFlosiLanguage provides locale.code
            ) {
                FlosiTheme {
                    FlosiAuthGate {
                        // AppSecurity inside FlosiApp is the single source of truth for
                        // PIN, biometrics, auto-lock and screenshot/recents protection.
                        // Keeping a second biometric gate here caused contradictory lock
                        // states because it used unrelated legacy DataStore preferences.
                        FlosiApp()
                    }
                }
            }
        }
    }
}
