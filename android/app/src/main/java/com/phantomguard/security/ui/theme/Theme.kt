package com.phantomguard.security.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceNavyElevated,
    onPrimaryContainer = CyanGlow,
    secondary = CyanAccent,
    onSecondary = DarkNavyBg,
    background = DarkNavyBg,
    onBackground = TextPrimary,
    surface = SurfaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceNavyElevated,
    onSurfaceVariant = TextSecondary,
    error = EmergencyRed,
    onError = TextPrimary
)

@Composable
fun PhantomGuardTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavyBg.toArgb()
            window.navigationBarColor = DarkNavyBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PhantomTypography,
        content = content
    )
}
