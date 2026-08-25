package com.flosi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.flosi.app.ui.components.*
import java.util.Locale

private val lightScheme = lightColorScheme(
    primary = FlosiPurple,
    onPrimary = Color.White,
    primaryContainer = FlosiPurpleSoft,
    onPrimaryContainer = FlosiText,
    secondary = FlosiBlue,
    tertiary = FlosiGreen,
    background = FlosiBg,
    onBackground = FlosiText,
    surface = FlosiSurface,
    onSurface = FlosiText,
    surfaceVariant = Color(0xFFF5F3F8),
    onSurfaceVariant = FlosiMuted,
    outline = FlosiLine,
    error = FlosiRed,
)

private val darkScheme = darkColorScheme(
    primary = Color(0xFFB99BFF),
    onPrimary = Color(0xFF24163E),
    primaryContainer = Color(0xFF3C2863),
    onPrimaryContainer = Color(0xFFF0E9FF),
    secondary = Color(0xFF8BCAFF),
    tertiary = Color(0xFF63DCAE),
    background = Color(0xFF111014),
    onBackground = Color(0xFFF7F3FA),
    surface = Color(0xFF19171E),
    onSurface = Color(0xFFF7F3FA),
    surfaceVariant = Color(0xFF24212B),
    onSurfaceVariant = Color(0xFFB8B1C0),
    outline = Color(0xFF3A3541),
    error = Color(0xFFFF8A90),
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

private fun isRtlLocale(locale: Locale): Boolean {
    return locale.language.lowercase() in setOf("ar", "fa", "ur", "he")
}

@Composable
fun FlosiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val locale = Locale.getDefault()
    val direction = if (isRtlLocale(locale)) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme else lightScheme,
            typography = typography,
            content = content,
        )
    }
}
