package com.flosi.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.fragment.app.FragmentActivity
import com.flosi.app.auth.FlosiAuthGate
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.subscription.FlosiSubscriptionGate
import com.flosi.app.ui.navigation.FlosiApp
import com.flosi.app.ui.theme.FlosiTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        normalizeSubscriptionClock()

        setContent {
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalFlosiLanguage provides "ar"
            ) {
                FlosiTheme {
                    FlosiAuthGate {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                        ) {
                            FlosiSubscriptionGate {
                                FlosiApp()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun normalizeSubscriptionClock() {
        val prefs = getSharedPreferences("flosi_subscription", MODE_PRIVATE)
        val stored = prefs.getLong("trusted_now", 0L)
        val normalizedStored = if (stored in 1L until 100_000_000_000L) stored * 1_000L else stored
        val now = System.currentTimeMillis()
        val candidate = maxOf(normalizedStored, now)
        prefs.edit().putLong("trusted_now", candidate).apply()
    }
}
