package com.healthcalendar.app.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.preferences.UserPreferencesRepository
import com.healthcalendar.app.data.repository.AppointmentRepository
import com.healthcalendar.app.data.repository.MedicationRepository
import com.healthcalendar.app.util.BackupManager
import com.healthcalendar.app.util.DataExporter
import com.healthcalendar.app.util.DataImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// Consolidated settings UI state for better performance
data class SettingsUiState(
    val themeMode: UserPreferencesRepository.ThemeMode = UserPreferencesRepository.ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val fontScale: Float = 1.0f,
    val highContrast: Boolean = false,
    val amoledMode: Boolean = false,
    val language: UserPreferencesRepository.Language = UserPreferencesRepository.Language.ENGLISH,
    val notificationSound: UserPreferencesRepository.NotificationSound = UserPreferencesRepository.NotificationSound.DEFAULT,
    val vibrate: Boolean = true,
    val autoBackup: Boolean = false,
    val backupFrequency: UserPreferencesRepository.BackupFrequency = UserPreferencesRepository.BackupFrequency.WEEKLY,
    val exportFormat: UserPreferencesRepository.ExportFormat = UserPreferencesRepository.ExportFormat.CSV,
    val passcodeEnabled: Boolean = false,
    val passcodeHash: String? = null,
    val biometricEnabled: Boolean = false,
    // New security fields
    val autoLockEnabled: Boolean = false,
    val autoLockTimeoutMinutes: Int = 0,
    val lockOnScreenOff: Boolean = false,
    val rememberUnlockMinutes: Int = 0,
    val maxFailedAttempts: Int = 5,
    val failedAttemptCount: Int = 0,
    val requireBiometricAfterFails: Boolean = false,
    val passcodeLength: Int = 6,
    val allowAlphanumeric: Boolean = false,
    val securityQuestion: String? = null,
    val recoveryEmail: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appointmentRepository: AppointmentRepository,
    private val medicationRepository: MedicationRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Consolidated settings state - reduces recompositions significantly
    val settingsState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.dynamicColor,
        preferencesRepository.fontScale,
        preferencesRepository.highContrast,
        preferencesRepository.amoledMode,
        preferencesRepository.language,
        preferencesRepository.notificationSound,
        preferencesRepository.vibrate,
        preferencesRepository.autoBackup,
        preferencesRepository.backupFrequency,
        preferencesRepository.exportFormat,
        preferencesRepository.passcodeEnabled,
        preferencesRepository.passcodeHash,
        preferencesRepository.biometricEnabled,
        preferencesRepository.autoLockEnabled,
        preferencesRepository.autoLockTimeoutMinutes,
        preferencesRepository.lockOnScreenOff,
        preferencesRepository.rememberUnlockMinutes,
        preferencesRepository.maxFailedAttempts,
        preferencesRepository.failedAttemptCount,
        preferencesRepository.requireBiometricAfterFails,
        preferencesRepository.passcodeLength,
        preferencesRepository.allowAlphanumeric,
        preferencesRepository.securityQuestion,
        preferencesRepository.recoveryEmail
    ) { values ->
        SettingsUiState(
            themeMode = values[0] as UserPreferencesRepository.ThemeMode,
            dynamicColor = values[1] as Boolean,
            fontScale = values[2] as Float,
            highContrast = values[3] as Boolean,
            amoledMode = values[4] as Boolean,
            language = values[5] as UserPreferencesRepository.Language,
            notificationSound = values[6] as UserPreferencesRepository.NotificationSound,
            vibrate = values[7] as Boolean,
            autoBackup = values[8] as Boolean,
            backupFrequency = values[9] as UserPreferencesRepository.BackupFrequency,
            exportFormat = values[10] as UserPreferencesRepository.ExportFormat,
            passcodeEnabled = values[11] as Boolean,
            passcodeHash = values[12] as String?,
            biometricEnabled = values[13] as Boolean,
            autoLockEnabled = values[14] as Boolean,
            autoLockTimeoutMinutes = values[15] as Int,
            lockOnScreenOff = values[16] as Boolean,
            rememberUnlockMinutes = values[17] as Int,
            maxFailedAttempts = values[18] as Int,
            failedAttemptCount = values[19] as Int,
            requireBiometricAfterFails = values[20] as Boolean,
            passcodeLength = values[21] as Int,
            allowAlphanumeric = values[22] as Boolean,
            securityQuestion = values[23] as String?,
            recoveryEmail = values[24] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    // Individual properties exposed as StateFlows for UI consumption
    val themeMode = preferencesRepository.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.ThemeMode.SYSTEM
    )
    val dynamicColor = preferencesRepository.dynamicColor.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), true
    )
    val fontScale = preferencesRepository.fontScale.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 1.0f
    )
    val highContrast = preferencesRepository.highContrast.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val amoledMode = preferencesRepository.amoledMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val language = preferencesRepository.language.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.Language.ENGLISH
    )
    val notificationSound = preferencesRepository.notificationSound.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.NotificationSound.DEFAULT
    )
    val vibrate = preferencesRepository.vibrate.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), true
    )
    val autoBackup = preferencesRepository.autoBackup.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val backupFrequency = preferencesRepository.backupFrequency.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.BackupFrequency.WEEKLY
    )
    val exportFormat = preferencesRepository.exportFormat.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.ExportFormat.CSV
    )
    val passcodeEnabled = preferencesRepository.passcodeEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val passcodeHash = preferencesRepository.passcodeHash.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), null
    )
    val biometricEnabled = preferencesRepository.biometricEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val autoLockEnabled = preferencesRepository.autoLockEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val autoLockTimeoutMinutes = preferencesRepository.autoLockTimeoutMinutes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 0
    )
    val lockOnScreenOff = preferencesRepository.lockOnScreenOff.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val maxFailedAttempts = preferencesRepository.maxFailedAttempts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 5
    )
    val failedAttemptCount = preferencesRepository.failedAttemptCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 0
    )
    val passcodeLength = preferencesRepository.passcodeLength.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 6
    )

    // Status flows
    private val _exportStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val exportStatus: StateFlow<OperationStatus> = _exportStatus.asStateFlow()
    
    private val _importStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val importStatus: StateFlow<OperationStatus> = _importStatus.asStateFlow()
    
    private val _backupStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val backupStatus: StateFlow<OperationStatus> = _backupStatus.asStateFlow()
    
    private val _backupList = MutableStateFlow<List<File>>(emptyList())
    val backupList: StateFlow<List<File>> = _backupList.asStateFlow()
    
    init {
        loadBackupList()
    }
    
    // Theme functions
    fun setThemeMode(mode: UserPreferencesRepository.ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }
    
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
        }
    }
    
    // Accessibility functions
    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            preferencesRepository.setFontScale(scale)
        }
    }
    
    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHighContrast(enabled)
        }
    }
    
    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAmoledMode(enabled)
        }
    }
    
    // Language function
    fun setLanguage(language: UserPreferencesRepository.Language) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }
    
    // Notification functions
    fun setNotificationSound(sound: UserPreferencesRepository.NotificationSound) {
        viewModelScope.launch {
            preferencesRepository.setNotificationSound(sound)
        }
    }
    
    fun setVibrate(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVibrate(enabled)
        }
    }
    
    // Backup functions
    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoBackup(enabled)
            if (enabled) {
                val hours = when (backupFrequency.value) {
                    UserPreferencesRepository.BackupFrequency.DAILY -> 24L
                    UserPreferencesRepository.BackupFrequency.WEEKLY -> 168L
                    UserPreferencesRepository.BackupFrequency.MONTHLY -> 720L
                }
                BackupManager.scheduleAutoBackup(context, hours)
            } else {
                BackupManager.cancelAutoBackup(context)
            }
        }
    }
    
    fun setBackupFrequency(frequency: UserPreferencesRepository.BackupFrequency) {
        viewModelScope.launch {
            preferencesRepository.setBackupFrequency(frequency)
            if (autoBackup.value) {
                val hours = when (frequency) {
                    UserPreferencesRepository.BackupFrequency.DAILY -> 24L
                    UserPreferencesRepository.BackupFrequency.WEEKLY -> 168L
                    UserPreferencesRepository.BackupFrequency.MONTHLY -> 720L
                }
                BackupManager.scheduleAutoBackup(context, hours)
            }
        }
    }
    
    fun setExportFormat(format: UserPreferencesRepository.ExportFormat) {
        viewModelScope.launch {
            preferencesRepository.setExportFormat(format)
        }
    }

    fun setPasscode(hash: String) {
        viewModelScope.launch {
            preferencesRepository.setPasscodeHash(hash)
            preferencesRepository.setPasscodeEnabled(true)
        }
    }

    fun disablePasscode() {
        viewModelScope.launch {
            preferencesRepository.clearPasscode()
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBiometricEnabled(enabled)
        }
    }
    
    // Export functions with format support
    fun exportAllData() {
        viewModelScope.launch {
            try {
                _exportStatus.value = OperationStatus.Loading
                
                val appointments = appointmentRepository.getAllAppointments().first()
                val medications = medicationRepository.medications.first()
                val logs = medicationRepository.getAllMedicationLogs()
                
                val format = exportFormat.value
                val content = when (format) {
                    UserPreferencesRepository.ExportFormat.CSV -> DataExporter.exportToCsv(appointments, medications, logs)
                    UserPreferencesRepository.ExportFormat.JSON -> DataExporter.exportToJson(appointments, medications, logs)
                    UserPreferencesRepository.ExportFormat.XML -> DataExporter.exportToXml(appointments, medications, logs)
                    UserPreferencesRepository.ExportFormat.PDF -> "PDF export not yet implemented"
                }
                
                val extension = DataExporter.getFileExtension(format.name)
                val mimeType = DataExporter.getMimeType(format.name)
                val file = createFile("health_calendar_export", content, extension)
                
                _exportStatus.value = OperationStatus.Success(file, mimeType)
            } catch (e: Exception) {
                _exportStatus.value = OperationStatus.Error(e.message ?: "Export failed")
            }
        }
    }
    
    fun exportAppointments() {
        viewModelScope.launch {
            try {
                _exportStatus.value = OperationStatus.Loading
                
                val appointments = appointmentRepository.getAllAppointments().first()
                val format = exportFormat.value
                val content = when (format) {
                    UserPreferencesRepository.ExportFormat.CSV -> DataExporter.exportAppointmentsToCsv(appointments)
                    UserPreferencesRepository.ExportFormat.JSON -> DataExporter.exportAppointmentsToJson(appointments)
                    else -> DataExporter.exportAppointmentsToCsv(appointments)
                }
                
                val extension = DataExporter.getFileExtension(format.name)
                val mimeType = DataExporter.getMimeType(format.name)
                val file = createFile("appointments_export", content, extension)
                
                _exportStatus.value = OperationStatus.Success(file, mimeType)
            } catch (e: Exception) {
                _exportStatus.value = OperationStatus.Error(e.message ?: "Export failed")
            }
        }
    }
    
    fun exportMedications() {
        viewModelScope.launch {
            try {
                _exportStatus.value = OperationStatus.Loading
                
                val medications = medicationRepository.medications.first()
                val format = exportFormat.value
                val content = when (format) {
                    UserPreferencesRepository.ExportFormat.CSV -> DataExporter.exportMedicationsToCsv(medications)
                    UserPreferencesRepository.ExportFormat.JSON -> DataExporter.exportMedicationsToJson(medications)
                    else -> DataExporter.exportMedicationsToCsv(medications)
                }
                
                val extension = DataExporter.getFileExtension(format.name)
                val mimeType = DataExporter.getMimeType(format.name)
                val file = createFile("medications_export", content, extension)
                
                _exportStatus.value = OperationStatus.Success(file, mimeType)
            } catch (e: Exception) {
                _exportStatus.value = OperationStatus.Error(e.message ?: "Export failed")
            }
        }
    }
    
    // Import function
    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                _importStatus.value = OperationStatus.Loading
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.readText() ?: ""
                inputStream?.close()
                
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val result = when {
                    mimeType.contains("json") || uri.path?.endsWith(".json") == true -> {
                        DataImporter.importFromJson(content)
                    }
                    mimeType.contains("csv") || uri.path?.endsWith(".csv") == true -> {
                        // Determine type from content or filename
                        DataImporter.importFromCsv(content, "APPOINTMENTS")
                    }
                    else -> {
                        DataImporter.importFromJson(content)
                    }
                }
                
                // Import data into database
                result.appointments.forEach { appointmentRepository.insertAppointment(it) }
                result.medications.forEach { medicationRepository.insertMedication(it) }
                
                val message = "Imported ${result.appointments.size} appointments, ${result.medications.size} medications"
                _importStatus.value = OperationStatus.Success(null, message)
            } catch (e: Exception) {
                _importStatus.value = OperationStatus.Error(e.message ?: "Import failed")
            }
        }
    }
    
    // Backup functions
    fun createBackup() {
        viewModelScope.launch {
            try {
                _backupStatus.value = OperationStatus.Loading
                
                val appointments = appointmentRepository.getAllAppointments().first()
                val medications = medicationRepository.medications.first()
                val logs = medicationRepository.getAllMedicationLogs()
                
                val backupFile = BackupManager.createEncryptedBackup(context, appointments, medications, logs)
                BackupManager.cleanOldBackups(context, 10)
                
                loadBackupList()
                _backupStatus.value = OperationStatus.Success(backupFile, "Backup created successfully")
            } catch (e: Exception) {
                _backupStatus.value = OperationStatus.Error(e.message ?: "Backup failed")
            }
        }
    }
    
    fun restoreBackup(backupFile: File) {
        viewModelScope.launch {
            try {
                _backupStatus.value = OperationStatus.Loading
                
                val backupData = BackupManager.restoreFromBackup(backupFile)
                if (backupData != null) {
                    // Import backup data
                    backupData.appointments.forEach { appointmentRepository.insertAppointment(it) }
                    backupData.medications.forEach { medicationRepository.insertMedication(it) }
                    
                    val message = "Restored ${backupData.appointments.size} appointments, ${backupData.medications.size} medications"
                    _backupStatus.value = OperationStatus.Success(null, message)
                } else {
                    _backupStatus.value = OperationStatus.Error("Failed to read backup file")
                }
            } catch (e: Exception) {
                _backupStatus.value = OperationStatus.Error(e.message ?: "Restore failed")
            }
        }
    }
    
    fun loadBackupList() {
        viewModelScope.launch {
            _backupList.value = BackupManager.listBackups(context)
        }
    }
    
    fun deleteBackup(backupFile: File) {
        viewModelScope.launch {
            backupFile.delete()
            loadBackupList()
        }
    }
    
    // File handling
    private fun createFile(baseName: String, content: String, extension: String): File {
        val timestamp = System.currentTimeMillis()
        val fileName = "${baseName}_$timestamp.$extension"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(content)
        return file
    }
    
    fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Share file")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
    
    fun saveExportToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val status = exportStatus.value
                if (status is OperationStatus.Success && status.file != null) {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    outputStream?.use { output ->
                        status.file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    // Delete temporary file
                    status.file.delete()
                    _exportStatus.value = OperationStatus.Idle
                }
            } catch (e: Exception) {
                _exportStatus.value = OperationStatus.Error("Failed to save file: ${e.message}")
            }
        }
    }
    
    fun resetExportStatus() {
        _exportStatus.value = OperationStatus.Idle
    }
    
    fun resetImportStatus() {
        _importStatus.value = OperationStatus.Idle
    }
    
    fun resetBackupStatus() {
        _backupStatus.value = OperationStatus.Idle
    }

    // Security settings setters
    fun setAutoLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoLockEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setAutoLockTimeoutMinutes(minutes)
        }
    }

    fun setLockOnScreenOff(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLockOnScreenOff(enabled)
        }
    }

    fun setMaxFailedAttempts(max: Int) {
        viewModelScope.launch {
            preferencesRepository.setMaxFailedAttempts(max)
        }
    }

    fun setPasscodeLength(length: Int) {
        viewModelScope.launch {
            preferencesRepository.setPasscodeLength(length)
        }
    }

    fun setAllowAlphanumeric(allowed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAllowAlphanumeric(allowed)
        }
    }

    fun setSecurityQuestion(question: String, answer: String) {
        viewModelScope.launch {
            val answerHash = sha256(answer.trim().lowercase())
            preferencesRepository.setSecurityQuestion(question, answerHash)
        }
    }

    fun setRecoveryEmail(email: String) {
        viewModelScope.launch {
            preferencesRepository.setRecoveryEmail(email)
        }
    }

    private fun sha256(input: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    sealed class OperationStatus {
        object Idle : OperationStatus()
        object Loading : OperationStatus()
        data class Success(val file: File?, val message: String = "") : OperationStatus()
        data class Error(val message: String) : OperationStatus()
    }
}
