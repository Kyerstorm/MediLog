package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.healthcalendar.app.ui.theme.AppTheme

/**
 * App settings entity for storing user preferences
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // Single row for settings
    val selectedTheme: String = AppTheme.DEFAULT.name,
    val isDarkMode: Boolean = false,
    val useDynamicColors: Boolean = false,
    val useCustomTheme: Boolean = false,
    val customThemeJson: String? = null,
    val notificationSoundUri: String? = null, // URI for custom notification sound
    val recentLocations: List<String> = emptyList() // Last 3 locations used
) {
    fun getTheme(): AppTheme {
        return try {
            AppTheme.valueOf(selectedTheme)
        } catch (e: IllegalArgumentException) {
            AppTheme.DEFAULT
        }
    }
}
