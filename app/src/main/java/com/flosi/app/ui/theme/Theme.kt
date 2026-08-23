package com.flosi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.flosi.app.ui.components.*

private val scheme = lightColorScheme(
    primary = FlosiPurple,
    onPrimary = Color.White,
    primaryContainer = FlosiPurpleSoft,
    onPrimaryContainer = FlosiText,
    secondary = FlosiBlue,
    tertiary = FlosiGreen,
    background = FlosiBg,
    onBackground = FlosiText,
    surface = Color.White,
    onSurface = FlosiText,
    surfaceVariant = Color(0xFFF5F3F8),
    outline = Color(0xFFE7E2ED),
    error = FlosiRed,
)

private val typography = Typography(
    displaySmall = Typography().displaySmall.copy(fontSize = 34.sp),
    headlineLarge = Typography().headlineLarge.copy(fontSize = 28.sp),
    headlineMedium = Typography().headlineMedium.copy(fontSize = 24.sp),
    titleLarge = Typography().titleLarge.copy(fontSize = 20.sp),
    titleMedium = Typography().titleMedium.copy(fontSize = 16.sp),
    bodyLarge = Typography().bodyLarge.copy(fontSize = 15.sp),
    bodyMedium = Typography().bodyMedium.copy(fontSize = 13.sp),
    labelLarge = Typography().labelLarge.copy(fontSize = 13.sp),
)

@Composable
fun FlosiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            content = content
        )
    }
}
