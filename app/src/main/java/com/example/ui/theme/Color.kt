package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass Dark Palette
val VoidBlack = Color(0xFF070B14)
val DarkGlassSurface = Color(0xCC0E172E)
val DarkGlassCard = Color(0x99131F3F)
val DarkGlassCardHover = Color(0xCC1A2B55)
val DarkGlassBorder = Color(0x4038BDF8)
val DarkGlassBorderSubtle = Color(0x2038BDF8)

// Neon & Accent Colors
val NeonCyan = Color(0xFF00F2FE)
val ElectricBlue = Color(0xFF4FACFE)
val CyanGlow = Color(0xFF38BDF8)
val DeepIndigo = Color(0xFF1E1B4B)
val GlassPurple = Color(0xFF8B5CF6)
val GlassViolet = Color(0xFF7C3AED)
val EmeraldMint = Color(0xFF10B981)
val EmeraldGlow = Color(0xFF34D399)
val CoralFlame = Color(0xFFF43F5E)
val AmberWarning = Color(0xFFF59E0B)
val LightGlassSurface = Color(0xE6F8FAFC)
val LightGlassCard = Color(0xD9FFFFFF)
val LightGlassBorder = Color(0x400284C7)

// Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextMutedLight = Color(0xFF94A3B8)

// Gradients
val CyanBlueGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, ElectricBlue)
)

val PurpleVioletGradient = Brush.linearGradient(
    colors = listOf(GlassPurple, GlassViolet)
)

val EmeraldTealGradient = Brush.linearGradient(
    colors = listOf(EmeraldMint, Color(0xFF06B6D4))
)

val CoralSunsetGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF5E62), CoralFlame)
)

val AmberSunGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFBBF24), AmberWarning)
)

val DarkGlassBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF070B16),
        Color(0xFF0B132B),
        Color(0xFF080D1D)
    )
)

val LightGlassBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF0F6FF),
        Color(0xFFE2EDFD),
        Color(0xFFF8FAFC)
    )
)
