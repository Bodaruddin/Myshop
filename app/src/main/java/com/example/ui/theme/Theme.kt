package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = VoidBlack,
    primaryContainer = Color(0xFF0E2A47),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricBlue,
    onSecondary = VoidBlack,
    secondaryContainer = Color(0xFF14244E),
    onSecondaryContainer = ElectricBlue,
    tertiary = GlassPurple,
    onTertiary = Color.White,
    background = VoidBlack,
    onBackground = TextPrimaryDark,
    surface = DarkGlassSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkGlassCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkGlassBorder,
    outlineVariant = DarkGlassBorderSubtle,
    error = CoralFlame,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF4338CA),
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = TextPrimaryLight,
    surface = LightGlassSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightGlassCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightGlassBorder,
    outlineVariant = Color(0x200284C7),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark theme for optimal liquid glass luminescence
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
