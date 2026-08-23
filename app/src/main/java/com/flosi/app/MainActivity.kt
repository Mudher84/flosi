package com.flosi.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.flosi.app.auth.FlosiAuthGate
import com.flosi.app.security.BiometricGate
import com.flosi.app.ui.navigation.FlosiApp
import com.flosi.app.ui.theme.FlosiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private var unlocked by mutableStateOf(false)
    private var gateEnabled by mutableStateOf(false)
    private var authStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val app = application as FlosiApplication
            val prefs = app.preferences.state.first()
            gateEnabled = prefs.biometricLock
            if (prefs.hideRecents) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (!gateEnabled) unlocked = true
        }

        setContent {
            FlosiTheme {
                FlosiAuthGate {
                    if (unlocked || !gateEnabled) {
                        FlosiApp()
                    } else {
                        LaunchedEffect(gateEnabled, unlocked) {
                            if (gateEnabled && !unlocked && !authStarted) launchBiometric()
                        }
                        LockedScreen { launchBiometric() }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun launchBiometric() {
        if (authStarted || !gateEnabled) return
        authStarted = true
        if (!BiometricGate.available(this)) {
            authStarted = false
            return
        }
        BiometricGate.authenticate(
            this,
            onSuccess = { unlocked = true; authStarted = false },
            onError = { authStarted = false }
        )
    }
}

@Composable
private fun LockedScreen(onUnlock: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("فلوسي", style = MaterialTheme.typography.headlineLarge)
            Text("التطبيق مقفول")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onUnlock) { Text("فتح بالبصمة أو الوجه") }
        }
    }
}
