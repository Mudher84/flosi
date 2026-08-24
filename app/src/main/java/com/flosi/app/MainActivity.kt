package com.flosi.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.LayoutDirection
import androidx.fragment.app.FragmentActivity
import com.flosi.app.auth.FlosiAuthGate
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.ui.navigation.FlosiApp
import com.flosi.app.ui.theme.FlosiTheme
import java.util.Locale

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LaunchedEffect(Unit) {
                Locale.setDefault(Locale.forLanguageTag("ar-IQ"))
            }

            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalFlosiLanguage provides "ar"
            ) {
                FlosiTheme {
                    FlosiAuthGate {
                        FlosiApp()
                    }
                }
            }
        }
    }
}
