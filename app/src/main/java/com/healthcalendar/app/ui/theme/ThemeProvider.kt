package com.healthcalendar.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App theme enum for different color schemes
 */
enum class AppTheme {
    DEFAULT,
    MEDICAL_GREEN,
    CALM_PURPLE,
    WARM_ORANGE,
    PROFESSIONAL_GRAY,
    OCEAN_BLUE,
    SUNSET_RED,
    FOREST_GREEN,
    HIGH_CONTRAST,
    AMOLED
}

/**
 * Theme color set
 */
data class ThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color
)

/**
 * Provides color schemes for different themes
 */
object ThemeProvider {
    
    fun getThemeColors(theme: AppTheme, isDark: Boolean): ThemeColors {
        return when (theme) {
            AppTheme.DEFAULT -> if (isDark) defaultDarkColors() else defaultLightColors()
            AppTheme.MEDICAL_GREEN -> if (isDark) medicalGreenDarkColors() else medicalGreenLightColors()
            AppTheme.CALM_PURPLE -> if (isDark) calmPurpleDarkColors() else calmPurpleLightColors()
            AppTheme.WARM_ORANGE -> if (isDark) warmOrangeDarkColors() else warmOrangeLightColors()
            AppTheme.PROFESSIONAL_GRAY -> if (isDark) professionalGrayDarkColors() else professionalGrayLightColors()
            AppTheme.OCEAN_BLUE -> if (isDark) oceanBlueDarkColors() else oceanBlueLightColors()
            AppTheme.SUNSET_RED -> if (isDark) sunsetRedDarkColors() else sunsetRedLightColors()
            AppTheme.FOREST_GREEN -> if (isDark) forestGreenDarkColors() else forestGreenLightColors()
            AppTheme.HIGH_CONTRAST -> if (isDark) highContrastDarkColors() else highContrastLightColors()
            AppTheme.AMOLED -> if (isDark) amoledDarkColors() else amoledLightColors()
        }
    }

    /**
     * Parse a JSON string containing hex color strings for theme slots into a ThemeColors object.
     * Any missing or invalid entries fall back to the provider defaults for the given appTheme/isDark.
     * Expected JSON keys (all optional): primary,onPrimary,primaryContainer,onPrimaryContainer,
     * secondary,onSecondary,secondaryContainer,onSecondaryContainer,tertiary,onTertiary,tertiaryContainer,onTertiaryContainer,
     * error,onError,errorContainer,onErrorContainer,background,onBackground,surface,onSurface,surfaceVariant,onSurfaceVariant,outline
     */
    fun parseThemeColorsFromJson(json: String, appTheme: AppTheme, isDark: Boolean): ThemeColors {
        val defaults = getThemeColors(appTheme, isDark)
        return try {
            val obj = org.json.JSONObject(json)
            fun getColor(key: String, fallback: androidx.compose.ui.graphics.Color) : androidx.compose.ui.graphics.Color {
                return if (obj.has(key)) {
                    try {
                        val s = obj.getString(key)
                        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(s))
                    } catch (e: Exception) {
                        fallback
                    }
                } else fallback
            }

            ThemeColors(
                primary = getColor("primary", defaults.primary),
                onPrimary = getColor("onPrimary", defaults.onPrimary),
                primaryContainer = getColor("primaryContainer", defaults.primaryContainer),
                onPrimaryContainer = getColor("onPrimaryContainer", defaults.onPrimaryContainer),
                secondary = getColor("secondary", defaults.secondary),
                onSecondary = getColor("onSecondary", defaults.onSecondary),
                secondaryContainer = getColor("secondaryContainer", defaults.secondaryContainer),
                onSecondaryContainer = getColor("onSecondaryContainer", defaults.onSecondaryContainer),
                tertiary = getColor("tertiary", defaults.tertiary),
                onTertiary = getColor("onTertiary", defaults.onTertiary),
                tertiaryContainer = getColor("tertiaryContainer", defaults.tertiaryContainer),
                onTertiaryContainer = getColor("onTertiaryContainer", defaults.onTertiaryContainer),
                error = getColor("error", defaults.error),
                onError = getColor("onError", defaults.onError),
                errorContainer = getColor("errorContainer", defaults.errorContainer),
                onErrorContainer = getColor("onErrorContainer", defaults.onErrorContainer),
                background = getColor("background", defaults.background),
                onBackground = getColor("onBackground", defaults.onBackground),
                surface = getColor("surface", defaults.surface),
                onSurface = getColor("onSurface", defaults.onSurface),
                surfaceVariant = getColor("surfaceVariant", defaults.surfaceVariant),
                onSurfaceVariant = getColor("onSurfaceVariant", defaults.onSurfaceVariant),
                outline = getColor("outline", defaults.outline)
            )
        } catch (e: Exception) {
            defaults
        }
    }
    
    // DEFAULT BLUE (Current theme)
    private fun defaultLightColors() = ThemeColors(
        primary = Color(0xFF2196F3),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF03A9F4),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFB3E5FC),
        onSecondaryContainer = Color(0xFF01579B),
        tertiary = Color(0xFF00BCD4),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFB2EBF2),
        onTertiaryContainer = Color(0xFF006064),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E)
    )
    
    private fun defaultDarkColors() = ThemeColors(
        primary = Color(0xFF64B5F6),
        onPrimary = Color(0xFF003258),
        primaryContainer = Color(0xFF004A77),
        onPrimaryContainer = Color(0xFFCCE5FF),
        secondary = Color(0xFF81D4FA),
        onSecondary = Color(0xFF00344F),
        secondaryContainer = Color(0xFF004C6F),
        onSecondaryContainer = Color(0xFFCFE6FF),
        tertiary = Color(0xFF80DEEA),
        onTertiary = Color(0xFF003A40),
        tertiaryContainer = Color(0xFF00525C),
        onTertiaryContainer = Color(0xFFCFF4FF),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99)
    )
    
    // MEDICAL GREEN
    private fun medicalGreenLightColors() = ThemeColors(
        primary = Color(0xFF4CAF50),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF66BB6A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDCEDC8),
        onSecondaryContainer = Color(0xFF33691E),
        tertiary = Color(0xFF81C784),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE8F5E9),
        onTertiaryContainer = Color(0xFF2E7D32),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFF1F8E9),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE8F5E9),
        onSurfaceVariant = Color(0xFF2E7D32),
        outline = Color(0xFF66BB6A)
    )
    
    private fun medicalGreenDarkColors() = ThemeColors(
        primary = Color(0xFF81C784),
        onPrimary = Color(0xFF003300),
        primaryContainer = Color(0xFF1B5E20),
        onPrimaryContainer = Color(0xFFC8E6C9),
        secondary = Color(0xFF9CCC65),
        onSecondary = Color(0xFF1B3300),
        secondaryContainer = Color(0xFF33691E),
        onSecondaryContainer = Color(0xFFDCEDC8),
        tertiary = Color(0xFFA5D6A7),
        onTertiary = Color(0xFF003300),
        tertiaryContainer = Color(0xFF2E7D32),
        onTertiaryContainer = Color(0xFFE8F5E9),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF2E3B2E),
        onSurfaceVariant = Color(0xFFC8E6C9),
        outline = Color(0xFF66BB6A)
    )
    
    // CALM PURPLE
    private fun calmPurpleLightColors() = ThemeColors(
        primary = Color(0xFF9C27B0),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE1BEE7),
        onPrimaryContainer = Color(0xFF4A148C),
        secondary = Color(0xFFAB47BC),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3E5F5),
        onSecondaryContainer = Color(0xFF6A1B9A),
        tertiary = Color(0xFFBA68C8),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF3E5F5),
        onTertiaryContainer = Color(0xFF7B1FA2),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFF3E5F5),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFF3E5F5),
        onSurfaceVariant = Color(0xFF7B1FA2),
        outline = Color(0xFFAB47BC)
    )
    
    private fun calmPurpleDarkColors() = ThemeColors(
        primary = Color(0xFFCE93D8),
        onPrimary = Color(0xFF38003C),
        primaryContainer = Color(0xFF6A1B9A),
        onPrimaryContainer = Color(0xFFF3E5F5),
        secondary = Color(0xFFBA68C8),
        onSecondary = Color(0xFF4A148C),
        secondaryContainer = Color(0xFF7B1FA2),
        onSecondaryContainer = Color(0xFFF3E5F5),
        tertiary = Color(0xFFE1BEE7),
        onTertiary = Color(0xFF6A1B9A),
        tertiaryContainer = Color(0xFF9C27B0),
        onTertiaryContainer = Color(0xFFF3E5F5),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF3A2C3E),
        onSurfaceVariant = Color(0xFFE1BEE7),
        outline = Color(0xFFBA68C8)
    )
    
    // WARM ORANGE
    private fun warmOrangeLightColors() = ThemeColors(
        primary = Color(0xFFFF9800),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFE0B2),
        onPrimaryContainer = Color(0xFFE65100),
        secondary = Color(0xFFFFB74D),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFE0B2),
        onSecondaryContainer = Color(0xFFF57C00),
        tertiary = Color(0xFFFFCC80),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFFFF3E0),
        onTertiaryContainer = Color(0xFFFF6F00),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFFF3E0),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFFFE0B2),
        onSurfaceVariant = Color(0xFFF57C00),
        outline = Color(0xFFFFB74D)
    )
    
    private fun warmOrangeDarkColors() = ThemeColors(
        primary = Color(0xFFFFB74D),
        onPrimary = Color(0xFF4A2C00),
        primaryContainer = Color(0xFFE65100),
        onPrimaryContainer = Color(0xFFFFE0B2),
        secondary = Color(0xFFFFCC80),
        onSecondary = Color(0xFF663C00),
        secondaryContainer = Color(0xFFF57C00),
        onSecondaryContainer = Color(0xFFFFE0B2),
        tertiary = Color(0xFFFFE082),
        onTertiary = Color(0xFF4A3800),
        tertiaryContainer = Color(0xFFFF6F00),
        onTertiaryContainer = Color(0xFFFFF3E0),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF3E3024),
        onSurfaceVariant = Color(0xFFFFE0B2),
        outline = Color(0xFFFFB74D)
    )
    
    // PROFESSIONAL GRAY
    private fun professionalGrayLightColors() = ThemeColors(
        primary = Color(0xFF607D8B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCFD8DC),
        onPrimaryContainer = Color(0xFF263238),
        secondary = Color(0xFF78909C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFECEFF1),
        onSecondaryContainer = Color(0xFF37474F),
        tertiary = Color(0xFF90A4AE),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFECEFF1),
        onTertiaryContainer = Color(0xFF455A64),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFECEFF1),
        onSurfaceVariant = Color(0xFF455A64),
        outline = Color(0xFF78909C)
    )
    
    private fun professionalGrayDarkColors() = ThemeColors(
        primary = Color(0xFF90A4AE),
        onPrimary = Color(0xFF1C262B),
        primaryContainer = Color(0xFF37474F),
        onPrimaryContainer = Color(0xFFCFD8DC),
        secondary = Color(0xFFB0BEC5),
        onSecondary = Color(0xFF263238),
        secondaryContainer = Color(0xFF455A64),
        onSecondaryContainer = Color(0xFFECEFF1),
        tertiary = Color(0xFFCFD8DC),
        onTertiary = Color(0xFF37474F),
        tertiaryContainer = Color(0xFF546E7A),
        onTertiaryContainer = Color(0xFFECEFF1),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF2B3539),
        onSurfaceVariant = Color(0xFFCFD8DC),
        outline = Color(0xFF90A4AE)
    )
    
    // OCEAN BLUE
    private fun oceanBlueLightColors() = ThemeColors(
        primary = Color(0xFF0277BD),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB3E5FC),
        onPrimaryContainer = Color(0xFF01579B),
        secondary = Color(0xFF0288D1),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF81D4FA),
        onSecondaryContainer = Color(0xFF006097),
        tertiary = Color(0xFF039BE5),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE1F5FE),
        onTertiaryContainer = Color(0xFF0277BD),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFE1F5FE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFB3E5FC),
        onSurfaceVariant = Color(0xFF01579B),
        outline = Color(0xFF0288D1)
    )
    
    private fun oceanBlueDarkColors() = ThemeColors(
        primary = Color(0xFF4FC3F7),
        onPrimary = Color(0xFF002A3A),
        primaryContainer = Color(0xFF006097),
        onPrimaryContainer = Color(0xFFB3E5FC),
        secondary = Color(0xFF29B6F6),
        onSecondary = Color(0xFF00344A),
        secondaryContainer = Color(0xFF0277BD),
        onSecondaryContainer = Color(0xFF81D4FA),
        tertiary = Color(0xFF81D4FA),
        onTertiary = Color(0xFF003E52),
        tertiaryContainer = Color(0xFF0288D1),
        onTertiaryContainer = Color(0xFFE1F5FE),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF1E3440),
        onSurfaceVariant = Color(0xFFB3E5FC),
        outline = Color(0xFF4FC3F7)
    )
    
    // SUNSET RED
    private fun sunsetRedLightColors() = ThemeColors(
        primary = Color(0xFFE91E63),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF8BBD0),
        onPrimaryContainer = Color(0xFF880E4F),
        secondary = Color(0xFFEC407A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFCE4EC),
        onSecondaryContainer = Color(0xFFC2185B),
        tertiary = Color(0xFFF48FB1),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFFCE4EC),
        onTertiaryContainer = Color(0xFFAD1457),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFCE4EC),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFF8BBD0),
        onSurfaceVariant = Color(0xFFC2185B),
        outline = Color(0xFFEC407A)
    )
    
    private fun sunsetRedDarkColors() = ThemeColors(
        primary = Color(0xFFF48FB1),
        onPrimary = Color(0xFF3C001D),
        primaryContainer = Color(0xFFC2185B),
        onPrimaryContainer = Color(0xFFF8BBD0),
        secondary = Color(0xFFF8BBD0),
        onSecondary = Color(0xFF5C0035),
        secondaryContainer = Color(0xFFAD1457),
        onSecondaryContainer = Color(0xFFFCE4EC),
        tertiary = Color(0xFFFCE4EC),
        onTertiary = Color(0xFF5C003A),
        tertiaryContainer = Color(0xFFEC407A),
        onTertiaryContainer = Color(0xFFFCE4EC),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF3D2730),
        onSurfaceVariant = Color(0xFFF8BBD0),
        outline = Color(0xFFF48FB1)
    )
    
    // FOREST GREEN
    private fun forestGreenLightColors() = ThemeColors(
        primary = Color(0xFF2E7D32),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF388E3C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFA5D6A7),
        onSecondaryContainer = Color(0xFF1B5E20),
        tertiary = Color(0xFF43A047),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDCEDC8),
        onTertiaryContainer = Color(0xFF2E7D32),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFE8F5E9),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFC8E6C9),
        onSurfaceVariant = Color(0xFF2E7D32),
        outline = Color(0xFF43A047)
    )
    
    private fun forestGreenDarkColors() = ThemeColors(
        primary = Color(0xFF66BB6A),
        onPrimary = Color(0xFF003300),
        primaryContainer = Color(0xFF2E7D32),
        onPrimaryContainer = Color(0xFFC8E6C9),
        secondary = Color(0xFF81C784),
        onSecondary = Color(0xFF1B3300),
        secondaryContainer = Color(0xFF388E3C),
        onSecondaryContainer = Color(0xFFA5D6A7),
        tertiary = Color(0xFF9CCC65),
        onTertiary = Color(0xFF1B3300),
        tertiaryContainer = Color(0xFF43A047),
        onTertiaryContainer = Color(0xFFDCEDC8),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1C1C),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF2A3B2A),
        onSurfaceVariant = Color(0xFFC8E6C9),
        outline = Color(0xFF66BB6A)
    )

    // HIGH CONTRAST - strong foreground/background separation
    private fun highContrastLightColors() = ThemeColors(
        primary = Color(0xFF000000),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFFFFF),
        onPrimaryContainer = Color(0xFF000000),
        secondary = Color(0xFF000000),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFFFFF),
        onSecondaryContainer = Color(0xFF000000),
        tertiary = Color(0xFF000000),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFFFFF),
        onTertiaryContainer = Color(0xFF000000),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF000000),
        surfaceVariant = Color(0xFFFFFFFF),
        onSurfaceVariant = Color(0xFF000000),
        outline = Color(0xFF000000)
    )

    private fun highContrastDarkColors() = ThemeColors(
        primary = Color(0xFFFFFFFF),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF000000),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF000000),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFFFFFFF),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF000000),
        onTertiaryContainer = Color(0xFFFFFFFF),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF000000),
        onSurfaceVariant = Color(0xFFFFFFFF),
        outline = Color(0xFFFFFFFF)
    )

    // AMOLED - pure black surfaces for OLED savings
    private fun amoledLightColors() = ThemeColors(
        primary = Color(0xFF000000),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDDDDDD),
        onPrimaryContainer = Color(0xFF000000),
        secondary = Color(0xFF000000),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDDDDDD),
        onSecondaryContainer = Color(0xFF000000),
        tertiary = Color(0xFF000000),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDDDDDD),
        onTertiaryContainer = Color(0xFF000000),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF000000),
        surfaceVariant = Color(0xFFEEEEEE),
        onSurfaceVariant = Color(0xFF000000),
        outline = Color(0xFF000000)
    )

    private fun amoledDarkColors() = ThemeColors(
        primary = Color(0xFF64B5F6),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF000000),
        onPrimaryContainer = Color(0xFFCCE5FF),
        secondary = Color(0xFF81D4FA),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF000000),
        onSecondaryContainer = Color(0xFFCFE6FF),
        tertiary = Color(0xFF80DEEA),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF000000),
        onTertiaryContainer = Color(0xFFCFF4FF),
        error = Color(0xFFCF6679),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF000000),
        onSurfaceVariant = Color(0xFFFFFFFF),
        outline = Color(0xFF6B778D)
    )
    
    fun getThemeName(theme: AppTheme): String {
        return when (theme) {
            AppTheme.DEFAULT -> "Default Blue"
            AppTheme.MEDICAL_GREEN -> "Medical Green"
            AppTheme.CALM_PURPLE -> "Calm Purple"
            AppTheme.WARM_ORANGE -> "Warm Orange"
            AppTheme.PROFESSIONAL_GRAY -> "Professional Gray"
            AppTheme.OCEAN_BLUE -> "Ocean Blue"
            AppTheme.SUNSET_RED -> "Sunset Red"
            AppTheme.FOREST_GREEN -> "Forest Green"
            AppTheme.HIGH_CONTRAST -> "High Contrast"
            AppTheme.AMOLED -> "AMOLED"
            else -> "Custom"
        }
    }
}
