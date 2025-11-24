package com.healthcalendar.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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

        // Auto-lock settings
        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("auto_lock_timeout_minutes")
        val LOCK_ON_SCREEN_OFF = booleanPreferencesKey("lock_on_screen_off")
        val REMEMBER_UNLOCK_MINUTES = intPreferencesKey("remember_unlock_minutes")

        // Failed attempt security
        val MAX_FAILED_ATTEMPTS = intPreferencesKey("max_failed_attempts")
        val FAILED_ATTEMPT_COUNT = intPreferencesKey("failed_attempt_count")
        val LOCKOUT_UNTIL_TIMESTAMP = stringPreferencesKey("lockout_until_timestamp")
        val REQUIRE_BIOMETRIC_AFTER_FAILS = booleanPreferencesKey("require_biometric_after_fails")

        // Emergency access
        val SECURITY_QUESTION = stringPreferencesKey("security_question")
        val SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        val RECOVERY_EMAIL = stringPreferencesKey("recovery_email")

        // Passcode options
        val PASSCODE_LENGTH = intPreferencesKey("passcode_length")
        val ALLOW_ALPHANUMERIC = booleanPreferencesKey("allow_alphanumeric")
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

    // Auto-lock settings
    val autoLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_LOCK_ENABLED] ?: false
    }

    val autoLockTimeoutMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT_MINUTES] ?: 0 // 0 = immediate
    }

    val lockOnScreenOff: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOCK_ON_SCREEN_OFF] ?: false
    }

    val rememberUnlockMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMEMBER_UNLOCK_MINUTES] ?: 0
    }

    // Failed attempt security
    val maxFailedAttempts: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MAX_FAILED_ATTEMPTS] ?: 5
    }

    val failedAttemptCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] ?: 0
    }

    val lockoutUntilTimestamp: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP]
    }

    val requireBiometricAfterFails: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REQUIRE_BIOMETRIC_AFTER_FAILS] ?: false
    }

    // Emergency access
    val securityQuestion: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SECURITY_QUESTION]
    }

    val securityAnswerHash: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SECURITY_ANSWER_HASH]
    }

    val recoveryEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECOVERY_EMAIL]
    }

    // Passcode options
    val passcodeLength: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PASSCODE_LENGTH] ?: 6
    }

    val allowAlphanumeric: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALLOW_ALPHANUMERIC] ?: false
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

    // Auto-lock setters
    suspend fun setAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setAutoLockTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT_MINUTES] = minutes
        }
    }

    suspend fun setLockOnScreenOff(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCK_ON_SCREEN_OFF] = enabled
        }
    }

    suspend fun setRememberUnlockMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMEMBER_UNLOCK_MINUTES] = minutes
        }
    }

    // Failed attempt setters
    suspend fun setMaxFailedAttempts(max: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_FAILED_ATTEMPTS] = max
        }
    }

    suspend fun incrementFailedAttempts() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] ?: 0
            preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] = current + 1
        }
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] = 0
            preferences.remove(PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP)
        }
    }

    suspend fun setLockoutUntil(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = timestamp
        }
    }

    suspend fun setRequireBiometricAfterFails(required: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REQUIRE_BIOMETRIC_AFTER_FAILS] = required
        }
    }

    // Emergency access setters
    suspend fun setSecurityQuestion(question: String, answerHash: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECURITY_QUESTION] = question
            preferences[PreferencesKeys.SECURITY_ANSWER_HASH] = answerHash
        }
    }

    suspend fun setRecoveryEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECOVERY_EMAIL] = email
        }
    }

    // Passcode options setters
    suspend fun setPasscodeLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_LENGTH] = length
        }
    }

    suspend fun setAllowAlphanumeric(allowed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_ALPHANUMERIC] = allowed
        }
    }
}
