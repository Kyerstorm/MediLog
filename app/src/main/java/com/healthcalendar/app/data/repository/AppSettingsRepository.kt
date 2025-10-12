package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.AppSettingsDao
import com.healthcalendar.app.data.database.entities.AppSettings
import com.healthcalendar.app.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for app settings
 */
@Singleton
class AppSettingsRepository @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) {
    
    val settings: Flow<AppSettings> = appSettingsDao.getSettings().map { it ?: getDefaultSettings() }
    
    suspend fun getSettingsOnce(): AppSettings {
        return appSettingsDao.getSettingsOnce() ?: getDefaultSettings()
    }
    
    suspend fun updateTheme(theme: AppTheme) {
        val current = getSettingsOnce()
        appSettingsDao.insertSettings(current.copy(selectedTheme = theme.name))
    }
    
    suspend fun updateDarkMode(isDark: Boolean) {
        val current = getSettingsOnce()
        appSettingsDao.insertSettings(current.copy(isDarkMode = isDark))
    }
    
    suspend fun updateDynamicColors(useDynamic: Boolean) {
        val current = getSettingsOnce()
        appSettingsDao.insertSettings(current.copy(useDynamicColors = useDynamic))
    }
    
    suspend fun updateNotificationSound(soundUri: String?) {
        val current = getSettingsOnce()
        appSettingsDao.insertSettings(current.copy(notificationSoundUri = soundUri))
    }

    suspend fun updateUseCustomTheme(useCustom: Boolean) {
        val current = getSettingsOnce()
        appSettingsDao.insertSettings(current.copy(useCustomTheme = useCustom))
    }

    suspend fun updateCustomThemeJson(json: String?) {
        val current = getSettingsOnce()
        appSettingsDao.insertSettings(current.copy(customThemeJson = json))
    }
    
    suspend fun addRecentLocation(location: String) {
        if (location.isBlank()) return
        
        val current = getSettingsOnce()
        val recentLocations = current.recentLocations.toMutableList()
        
        // Remove if already exists (to move to front)
        recentLocations.remove(location)
        
        // Add to front
        recentLocations.add(0, location)
        
        // Keep only last 3
        val updatedLocations = recentLocations.take(3)
        
        appSettingsDao.insertSettings(current.copy(recentLocations = updatedLocations))
    }
    
    suspend fun initializeDefaultSettings() {
        if (appSettingsDao.getSettingsOnce() == null) {
            appSettingsDao.insertSettings(getDefaultSettings())
        }
    }
    
    private fun getDefaultSettings(): AppSettings {
        return AppSettings(
            id = 1,
            selectedTheme = AppTheme.DEFAULT.name,
            isDarkMode = false,
            useDynamicColors = false,
            notificationSoundUri = null,
            recentLocations = emptyList()
        )
    }
}
