package com.healthcalendar.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val PASSCODE_ENABLED = booleanPreferencesKey("passcode_enabled")
        val PASSCODE_HASH = stringPreferencesKey("passcode_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }
    
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        SPANISH("es", "Español"),
        FRENCH("fr", "Français"),
        GERMAN("de", "Deutsch"),
        ITALIAN("it", "Italiano"),
        PORTUGUESE("pt", "Português"),
        CHINESE("zh", "中文"),
        JAPANESE("ja", "日本語"),
        KOREAN("ko", "한국어")
    }
    
    enum class BackupFrequency {
        DAILY, WEEKLY, MONTHLY
    }
    
    enum class ExportFormat {
        CSV, JSON, XML, PDF
    }
    
    enum class NotificationSound {
        DEFAULT, GENTLE, ALERT, SILENT
    }
    
    // Theme
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(themeModeString)
    }
    
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
    }
    
    // Accessibility
    val fontScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SCALE] ?: 1.0f
    }
    
    val highContrast: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIGH_CONTRAST] ?: false
    }
    
    val amoledMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AMOLED_MODE] ?: false
    }
    
    // Language
    val language: Flow<Language> = context.dataStore.data.map { preferences ->
        val langCode = preferences[PreferencesKeys.LANGUAGE] ?: Language.ENGLISH.code
        Language.values().find { it.code == langCode } ?: Language.ENGLISH
    }
    
    // Notifications
    val notificationSound: Flow<NotificationSound> = context.dataStore.data.map { preferences ->
        val soundString = preferences[PreferencesKeys.NOTIFICATION_SOUND] ?: NotificationSound.DEFAULT.name
        NotificationSound.valueOf(soundString)
    }
    
    val vibrate: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATE] ?: true
    }
    
    // Backup
    val autoBackup: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_BACKUP] ?: false
    }
    
    val backupFrequency: Flow<BackupFrequency> = context.dataStore.data.map { preferences ->
        val freqString = preferences[PreferencesKeys.BACKUP_FREQUENCY] ?: BackupFrequency.WEEKLY.name
        BackupFrequency.valueOf(freqString)
    }
    
    // Export
    val exportFormat: Flow<ExportFormat> = context.dataStore.data.map { preferences ->
        val formatString = preferences[PreferencesKeys.EXPORT_FORMAT] ?: ExportFormat.CSV.name
        ExportFormat.valueOf(formatString)
    }

    // Security - passcode
    val passcodeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PASSCODE_ENABLED] ?: false
    }

    val passcodeHash: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PASSCODE_HASH]
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
    }
    
    // Setters
    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }
    
    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }
    
    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] = scale
        }
    }
    
    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST] = enabled
        }
    }
    
    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AMOLED_MODE] = enabled
        }
    }
    
    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }
    
    suspend fun setNotificationSound(sound: NotificationSound) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_SOUND] = sound.name
        }
    }
    
    suspend fun setVibrate(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATE] = enabled
        }
    }
    
    suspend fun setAutoBackup(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP] = enabled
        }
    }
    
    suspend fun setBackupFrequency(frequency: BackupFrequency) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKUP_FREQUENCY] = frequency.name
        }
    }
    
    suspend fun setExportFormat(format: ExportFormat) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPORT_FORMAT] = format.name
        }
    }

    // Security setters
    suspend fun setPasscodeHash(hash: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_HASH] = hash
        }
    }

    suspend fun setPasscodeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun clearPasscode() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PASSCODE_HASH)
            preferences[PreferencesKeys.PASSCODE_ENABLED] = false
        }
    }
}
