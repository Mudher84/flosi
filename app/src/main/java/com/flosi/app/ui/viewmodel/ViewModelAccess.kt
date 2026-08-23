package com.flosi.app.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flosi.app.FlosiApplication
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.settings.FlosiPreferences

@Composable
fun rememberFlosiRepository(): FinanceRepository {
    val app = LocalContext.current.applicationContext as FlosiApplication
    return remember(app) { app.repository }
}

@Composable
fun rememberFlosiPreferences(): FlosiPreferences {
    val app = LocalContext.current.applicationContext as FlosiApplication
    return remember(app) { app.preferences }
}

@Composable
inline fun <reified VM : ViewModel> flosiViewModel(): VM {
    val app = LocalContext.current.applicationContext as FlosiApplication
    val factory = remember(app) { FlosiVmFactory(app.repository) }
    return viewModel(factory = factory)
}
