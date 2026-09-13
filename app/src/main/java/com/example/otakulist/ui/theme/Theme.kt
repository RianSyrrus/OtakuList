package com.example.otakulist.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.otakulist.data.local.AppThemeMode

val LocalOtakuColors = staticCompositionLocalOf { darkOtakuColors() }

object OtakuTheme {
    val colors: OtakuColorScheme
        @Composable
        get() = LocalOtakuColors.current
}

@Composable
fun OtakuListTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentColor: Color = DefaultPrimaryAccent,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val otakuColors = if (isDark) {
        darkOtakuColors(accent = accentColor)
    } else {
        lightOtakuColors(accent = accentColor)
    }

    val materialColorScheme = if (isDark) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = otakuColors.onPrimary,
            primaryContainer = otakuColors.primaryContainer,
            onPrimaryContainer = otakuColors.onPrimaryContainer,
            background = otakuColors.canvasBackground,
            onBackground = otakuColors.onBackground,
            surface = otakuColors.surfaceBase,
            onSurface = otakuColors.onSurface,
            surfaceVariant = otakuColors.surfaceVariant,
            onSurfaceVariant = otakuColors.onSurfaceVariant,
            surfaceContainerLowest = otakuColors.surfaceContainerLowest,
            surfaceContainerLow = otakuColors.surfaceContainerLow,
            surfaceContainer = otakuColors.surfaceContainer,
            surfaceContainerHigh = otakuColors.surfaceContainerHigh,
            surfaceContainerHighest = otakuColors.surfaceContainerHighest,
            outline = otakuColors.outline,
            outlineVariant = otakuColors.outlineVariant
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = otakuColors.onPrimary,
            primaryContainer = otakuColors.primaryContainer,
            onPrimaryContainer = otakuColors.onPrimaryContainer,
            background = otakuColors.canvasBackground,
            onBackground = otakuColors.onBackground,
            surface = otakuColors.surfaceBase,
            onSurface = otakuColors.onSurface,
            surfaceVariant = otakuColors.surfaceVariant,
            onSurfaceVariant = otakuColors.onSurfaceVariant,
            surfaceContainerLowest = otakuColors.surfaceContainerLowest,
            surfaceContainerLow = otakuColors.surfaceContainerLow,
            surfaceContainer = otakuColors.surfaceContainer,
            surfaceContainerHigh = otakuColors.surfaceContainerHigh,
            surfaceContainerHighest = otakuColors.surfaceContainerHighest,
            outline = otakuColors.outline,
            outlineVariant = otakuColors.outlineVariant
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalOtakuColors provides otakuColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            shapes = Shapes
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides Typography.bodyMedium,
                content = content
            )
        }
    }
}