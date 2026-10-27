package com.example.otakulist.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Default Fallback Constants (Non-Composable)
val DefaultPrimaryAccent = Color(0xFF5B6EE8)
val DefaultCanvasBackground = Color(0xFF11131B)
val DefaultSurfaceBase = Color(0xFF1C1F2A)

// Base & Surface (Dynamic @Composable tokens delegating to OtakuTheme.colors)
val CanvasBackground: Color @Composable get() = OtakuTheme.colors.canvasBackground
val SurfaceBase: Color @Composable get() = OtakuTheme.colors.surfaceBase
val SurfaceDim: Color @Composable get() = OtakuTheme.colors.surfaceDim
val SurfaceBright: Color @Composable get() = OtakuTheme.colors.surfaceBright
val SurfaceContainerLowest: Color @Composable get() = OtakuTheme.colors.surfaceContainerLowest
val SurfaceContainerLow: Color @Composable get() = OtakuTheme.colors.surfaceContainerLow
val SurfaceContainer: Color @Composable get() = OtakuTheme.colors.surfaceContainer
val SurfaceContainerHigh: Color @Composable get() = OtakuTheme.colors.surfaceContainerHigh
val SurfaceContainerHighest: Color @Composable get() = OtakuTheme.colors.surfaceContainerHighest
val SurfaceVariant: Color @Composable get() = OtakuTheme.colors.surfaceVariant

// Teks di atas Surface (Dynamic @Composable tokens)
val OnBackground: Color @Composable get() = OtakuTheme.colors.onBackground
val OnSurface: Color @Composable get() = OtakuTheme.colors.onSurface
val OnSurfaceVariant: Color @Composable get() = OtakuTheme.colors.onSurfaceVariant
val OnSurfaceMuted: Color @Composable get() = OtakuTheme.colors.onSurfaceMuted
val InverseSurface: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFFE1E1ED) else Color(0xFF2E3039)
val InverseOnSurface: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFF2E3039) else Color(0xFFE1E1ED)

// Accent & Primary (Dynamic @Composable tokens)
val PrimaryAccent: Color @Composable get() = OtakuTheme.colors.primary
val PrimaryMaterial: Color @Composable get() = OtakuTheme.colors.primary
val OnPrimary: Color @Composable get() = OtakuTheme.colors.onPrimary
val PrimaryContainer: Color @Composable get() = OtakuTheme.colors.primaryContainer
val OnPrimaryContainer: Color @Composable get() = OtakuTheme.colors.onPrimaryContainer
val InversePrimary: Color @Composable get() = OtakuTheme.colors.primary

// Secondary & Tertiary
val Secondary: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFFC3C6D8) else Color(0xFF535B70)
val OnSecondary: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFF2C303E) else Color(0xFFFFFFFF)
val SecondaryContainer: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFF454958) else Color(0xFFD8DCF0)
val OnSecondaryContainer: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFFB4B7CA) else Color(0xFF10172A)
val Tertiary = Color(0xFFFFB77E)
val OnTertiary = Color(0xFF4D2600)
val TertiaryContainer = Color(0xFFD57A20)
val OnTertiaryContainer = Color(0xFF432100)

// Error & Outline
val Error: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
val OnError: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFF690005) else Color(0xFFFFFFFF)
val ErrorContainer: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFF93000A) else Color(0xFFFFDAD6)
val OnErrorContainer: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFFFFDAD6) else Color(0xFF410002)
val Outline: Color @Composable get() = OtakuTheme.colors.outline
val OutlineVariant: Color @Composable get() = OtakuTheme.colors.outlineVariant

// Status Aplikasi (Watchlist)
val StatusWatching: Color @Composable get() = OtakuTheme.colors.primary
val StatusCompleted: Color @Composable get() = if (OtakuTheme.colors.isDark) Color(0xFF4CAF7D) else Color(0xFF2E7D32)
val StatusDropped: Color @Composable get() = OtakuTheme.colors.onSurfaceMuted
val StatusPlanToWatch: Color @Composable get() = OtakuTheme.colors.onSurfaceMuted

// Dynamic Color Scheme
data class OtakuColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val canvasBackground: Color,
    val surfaceBase: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onSurfaceMuted: Color,
    val outline: Color,
    val outlineVariant: Color,
    val isDark: Boolean
)

fun darkOtakuColors(accent: Color = DefaultPrimaryAccent): OtakuColorScheme {
    return OtakuColorScheme(
        primary = accent,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = accent.copy(alpha = 0.25f),
        onPrimaryContainer = Color(0xFFEAEAF0),
        canvasBackground = Color(0xFF11131B),
        surfaceBase = Color(0xFF1C1F2A),
        surfaceDim = Color(0xFF11131B),
        surfaceBright = Color(0xFF373942),
        surfaceContainerLowest = Color(0xFF0C0E16),
        surfaceContainerLow = Color(0xFF191B23),
        surfaceContainer = Color(0xFF1D1F28),
        surfaceContainerHigh = Color(0xFF282A32),
        surfaceContainerHighest = Color(0xFF33343D),
        surfaceVariant = Color(0xFF262A38),
        onBackground = Color(0xFFEAEAF0),
        onSurface = Color(0xFFE1E1ED),
        onSurfaceVariant = Color(0xFFC5C5D6),
        onSurfaceMuted = Color(0xFF9498A8),
        outline = Color(0xFF33374A),
        outlineVariant = Color(0xFF454654),
        isDark = true
    )
}

fun lightOtakuColors(accent: Color = DefaultPrimaryAccent): OtakuColorScheme {
    return OtakuColorScheme(
        primary = accent,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = accent.copy(alpha = 0.15f),
        onPrimaryContainer = accent,
        canvasBackground = Color(0xFFF6F7FB),
        surfaceBase = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFECEEF4),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F6F9),
        surfaceContainer = Color(0xFFEFF1F7),
        surfaceContainerHigh = Color(0xFFE4E7F0),
        surfaceContainerHighest = Color(0xFFD9DDE8),
        surfaceVariant = Color(0xFFE8ECF5),
        onBackground = Color(0xFF15171F),
        onSurface = Color(0xFF1A1C24),
        onSurfaceVariant = Color(0xFF4C5062),
        onSurfaceMuted = Color(0xFF757A8E),
        outline = Color(0xFFD3D8E5),
        outlineVariant = Color(0xFFE2E6F0),
        isDark = false
    )
}

data class AccentPreset(
    val name: String,
    val color: Color,
    val hex: String
)

val AccentPresets = listOf(
    AccentPreset("Indigo Mewah", Color(0xFF5B6EE8), "#5B6EE8"),
    AccentPreset("Sakura Pink", Color(0xFFE01A6F), "#E01A6F"),
    AccentPreset("Emerald Mint", Color(0xFF00B894), "#00B894"),
    AccentPreset("Sunset Amber", Color(0xFFFD9644), "#FD9644"),
    AccentPreset("Cyberpunk Violet", Color(0xFF9B59B6), "#9B59B6"),
    AccentPreset("Crimson Red", Color(0xFFEB3B5A), "#EB3B5A"),
    AccentPreset("Ocean Cyan", Color(0xFF00CEC9), "#00CEC9")
)

fun parseHexColor(hex: String, fallback: Color = DefaultPrimaryAccent): Color {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> Color(android.graphics.Color.parseColor("#$clean"))
            8 -> Color(android.graphics.Color.parseColor("#$clean"))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}