package com.healthcalendar.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthcalendar.app.data.database.entities.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * DAO for app settings
 */
@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
    
    @Update
    suspend fun updateSettings(settings: AppSettings)
    
    @Query("UPDATE app_settings SET selectedTheme = :themeName WHERE id = 1")
    suspend fun updateTheme(themeName: String)
    
    @Query("UPDATE app_settings SET isDarkMode = :isDark WHERE id = 1")
    suspend fun updateDarkMode(isDark: Boolean)
    
    @Query("UPDATE app_settings SET useDynamicColors = :useDynamic WHERE id = 1")
    suspend fun updateDynamicColors(useDynamic: Boolean)
    
    @Query("UPDATE app_settings SET notificationSoundUri = :soundUri WHERE id = 1")
    suspend fun updateNotificationSound(soundUri: String?)

    @Query("UPDATE app_settings SET useCustomTheme = :useCustom WHERE id = 1")
    suspend fun updateUseCustomTheme(useCustom: Boolean)

    @Query("UPDATE app_settings SET customThemeJson = :json WHERE id = 1")
    suspend fun updateCustomThemeJson(json: String?)
}
