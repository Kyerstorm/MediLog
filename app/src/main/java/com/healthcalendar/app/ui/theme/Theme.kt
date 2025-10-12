package com.healthcalendar.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun HealthCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.DEFAULT,
    dynamicColor: Boolean = false, // Disabled by default to use custom themes
    customColors: ThemeColors? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            // Use provider themes or supplied customColors
            val themeColors = customColors ?: ThemeProvider.getThemeColors(appTheme, darkTheme)
            if (darkTheme) {
                darkColorScheme(
                    primary = themeColors.primary,
                    onPrimary = themeColors.onPrimary,
                    primaryContainer = themeColors.primaryContainer,
                    onPrimaryContainer = themeColors.onPrimaryContainer,
                    secondary = themeColors.secondary,
                    onSecondary = themeColors.onSecondary,
                    secondaryContainer = themeColors.secondaryContainer,
                    onSecondaryContainer = themeColors.onSecondaryContainer,
                    tertiary = themeColors.tertiary,
                    onTertiary = themeColors.onTertiary,
                    tertiaryContainer = themeColors.tertiaryContainer,
                    onTertiaryContainer = themeColors.onTertiaryContainer,
                    error = themeColors.error,
                    onError = themeColors.onError,
                    errorContainer = themeColors.errorContainer,
                    onErrorContainer = themeColors.onErrorContainer,
                    background = themeColors.background,
                    onBackground = themeColors.onBackground,
                    surface = themeColors.surface,
                    onSurface = themeColors.onSurface,
                    surfaceVariant = themeColors.surfaceVariant,
                    onSurfaceVariant = themeColors.onSurfaceVariant,
                    outline = themeColors.outline
                )
            } else {
                lightColorScheme(
                    primary = themeColors.primary,
                    onPrimary = themeColors.onPrimary,
                    primaryContainer = themeColors.primaryContainer,
                    onPrimaryContainer = themeColors.onPrimaryContainer,
                    secondary = themeColors.secondary,
                    onSecondary = themeColors.onSecondary,
                    secondaryContainer = themeColors.secondaryContainer,
                    onSecondaryContainer = themeColors.onSecondaryContainer,
                    tertiary = themeColors.tertiary,
                    onTertiary = themeColors.onTertiary,
                    tertiaryContainer = themeColors.tertiaryContainer,
                    onTertiaryContainer = themeColors.onTertiaryContainer,
                    error = themeColors.error,
                    onError = themeColors.onError,
                    errorContainer = themeColors.errorContainer,
                    onErrorContainer = themeColors.onErrorContainer,
                    background = themeColors.background,
                    onBackground = themeColors.onBackground,
                    surface = themeColors.surface,
                    onSurface = themeColors.onSurface,
                    surfaceVariant = themeColors.surfaceVariant,
                    onSurfaceVariant = themeColors.onSurfaceVariant,
                    outline = themeColors.outline
                )
            }
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
