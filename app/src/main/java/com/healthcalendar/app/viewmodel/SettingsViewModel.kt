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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appointmentRepository: AppointmentRepository,
    private val medicationRepository: MedicationRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    // Theme settings
    val themeMode = preferencesRepository.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.ThemeMode.SYSTEM
    )
    val dynamicColor = preferencesRepository.dynamicColor.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), true
    )
    
    // Accessibility settings
    val fontScale = preferencesRepository.fontScale.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 1.0f
    )
    val highContrast = preferencesRepository.highContrast.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val amoledMode = preferencesRepository.amoledMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    
    // Language setting
    val language = preferencesRepository.language.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.Language.ENGLISH
    )
    
    // Notification settings
    val notificationSound = preferencesRepository.notificationSound.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.NotificationSound.DEFAULT
    )
    val vibrate = preferencesRepository.vibrate.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), true
    )
    
    // Backup settings
    val autoBackup = preferencesRepository.autoBackup.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val backupFrequency = preferencesRepository.backupFrequency.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.BackupFrequency.WEEKLY
    )
    
    // Export format
    val exportFormat = preferencesRepository.exportFormat.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), UserPreferencesRepository.ExportFormat.CSV
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
    
    sealed class OperationStatus {
        object Idle : OperationStatus()
        object Loading : OperationStatus()
        data class Success(val file: File?, val message: String = "") : OperationStatus()
        data class Error(val message: String) : OperationStatus()
    }
}
