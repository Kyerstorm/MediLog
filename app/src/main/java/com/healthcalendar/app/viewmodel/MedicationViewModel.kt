package com.healthcalendar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.AppointmentCategory
import com.healthcalendar.app.data.database.entities.EventType
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationForm
import com.healthcalendar.app.data.database.entities.MedicationReminder
import com.healthcalendar.app.data.database.entities.MedicationSchedule
import com.healthcalendar.app.data.database.entities.RecurringPattern
import com.healthcalendar.app.data.database.entities.ScheduleFrequency
import com.healthcalendar.app.data.repository.AppointmentRepository
import com.healthcalendar.app.data.repository.MedicationRepository
import com.healthcalendar.app.data.repository.MedicationScheduleRepository
import com.healthcalendar.app.data.repository.MedicationLogRepository
import com.healthcalendar.app.data.repository.MedicationReminderRepository
import com.healthcalendar.app.util.alarm.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: MedicationScheduleRepository,
    private val appointmentRepository: AppointmentRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val medicationReminderRepository: MedicationReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    
    val medications: StateFlow<List<Medication>> = medicationRepository.getAllActiveMedications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Full medication list (including discontinued) for screens that need it
    val allMedications: StateFlow<List<Medication>> = medicationRepository.getAllMedications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedMedication = MutableStateFlow<Medication?>(null)
    val selectedMedication: StateFlow<Medication?> = _selectedMedication.asStateFlow()
    
    private val _medicationSchedules = MutableStateFlow<List<MedicationSchedule>>(emptyList())
    val medicationSchedules: StateFlow<List<MedicationSchedule>> = _medicationSchedules.asStateFlow()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Show/hide discontinued medications toggle persisted in ViewModel
    private val _showDiscontinued = MutableStateFlow(false)
    val showDiscontinued: StateFlow<Boolean> = _showDiscontinued.asStateFlow()

    fun toggleShowDiscontinued() {
        _showDiscontinued.value = !_showDiscontinued.value
    }

    fun setShowDiscontinued(value: Boolean) {
        _showDiscontinued.value = value
    }

    suspend fun getMedicationsByIds(medicationIds: List<Long>): List<Medication> {
        return medicationRepository.getMedicationsByIds(medicationIds)
    }

    fun loadMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val medication = medicationRepository.getMedicationById(medicationId)
                _selectedMedication.value = medication
                
                medication?.let {
                    scheduleRepository.getSchedulesForMedication(medicationId).collect { schedules ->
                        _medicationSchedules.value = schedules
                    }
                }
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun saveMedication(
        id: Long? = null,
        name: String,
        dosage: String,
        amount: String = "1",
        unit: String,
        form: MedicationForm,
        instructions: String,
        notes: String,
        isActive: Boolean = true,
        photoUri: String? = null,
        enableReminder: Boolean = false,
        reminderTime: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val medication = Medication(
                    id = id ?: 0,
                    name = name,
                    dosage = dosage,
                    amount = amount,
                    unit = unit,
                    form = form,
                    instructions = instructions,
                    notes = notes,
                    photoUri = photoUri,
                    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    isActive = isActive
                )
                
                val savedMedicationId = medicationRepository.insertMedication(medication)
                
                // Create reminder if enabled
                if (enableReminder && !reminderTime.isNullOrEmpty()) {
                    val timeParts = reminderTime.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toIntOrNull() ?: 9
                        val minute = timeParts[1].toIntOrNull() ?: 0
                        
                        // Create daily schedule
                        val schedule = MedicationSchedule(
                            medicationId = savedMedicationId,
                            frequency = ScheduleFrequency.DAILY,
                            time = LocalTime(hour, minute),
                            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7) // Every day
                        )
                        
                        val scheduleId = scheduleRepository.insertSchedule(schedule)
                        
                        // Schedule alarm
                        alarmScheduler.scheduleAlarm(schedule)
                        
                        // Create MedicationReminder for the Alarms tab (starting tomorrow to avoid showing past reminders)
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val tomorrow = today.plus(1, DateTimeUnit.DAY)
                        
                        val reminder = MedicationReminder(
                            medicationId = savedMedicationId,
                            date = tomorrow,
                            time = LocalTime(hour, minute),
                            dosage = "$dosage $unit",
                            instructions = instructions,
                            isRecurring = true,
                            recurringDays = listOf(0, 1, 2, 3, 4, 5, 6), // All days (0=Sunday, 6=Saturday)
                            alarmEnabled = true,
                            notes = "Daily medication reminder"
                        )
                        
                        medicationReminderRepository.insertReminder(reminder)
                    }
                }
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error saving medication")
            }
        }
    }
    
    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Cancel all alarms for this medication
                val schedules = scheduleRepository.getSchedulesForMedicationSync(medication.id)
                schedules.forEach { schedule ->
                    alarmScheduler.cancelAlarm(schedule.id)
                }
                
                // Delete alarms linked to this medication
                deleteAlarmsForMedication(medication.id)
                
                // Delete schedules and medication
                scheduleRepository.deleteSchedulesForMedication(medication.id)
                medicationRepository.deleteMedication(medication)
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error deleting medication")
            }
        }
    }
    
    fun addSchedule(schedule: MedicationSchedule) {
        viewModelScope.launch {
            try {
                val scheduleId = scheduleRepository.insertSchedule(schedule)
                val newSchedule = schedule.copy(id = scheduleId)
                alarmScheduler.scheduleAlarm(newSchedule)
                
                // Create corresponding alarm in Alarms tab
                createAlarmForSchedule(newSchedule)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error adding schedule")
            }
        }
    }
    
    private suspend fun createAlarmForSchedule(schedule: MedicationSchedule) {
        try {
            // Get medication details
            val medication = medicationRepository.getMedicationById(schedule.medicationId)
            if (medication == null) {
                return
            }
            
            // Get current date/time for alarm creation
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today = now.date
            
            // Create alarm title with medication name
            val title = "Take ${medication.name}"
            val description = "${medication.amount} x ${medication.dosage} ${medication.unit} - ${medication.form.name.lowercase().replaceFirstChar { it.uppercase() }}"
            
            // Determine recurring pattern based on schedule frequency
            val recurringPattern = when (schedule.frequency) {
                ScheduleFrequency.DAILY -> RecurringPattern.DAILY
                ScheduleFrequency.WEEKLY -> RecurringPattern.WEEKLY
                ScheduleFrequency.AS_NEEDED -> null
                ScheduleFrequency.CUSTOM -> if (schedule.daysOfWeek.isNotEmpty()) RecurringPattern.WEEKLY else null
            }
            
            // Create start time (today at scheduled time)
            val startTime = LocalDateTime(
                year = today.year,
                monthNumber = today.monthNumber,
                dayOfMonth = today.dayOfMonth,
                hour = schedule.time.hour,
                minute = schedule.time.minute,
                second = 0
            )
            
            // End time is same as start time for alarms
            val endTime = startTime
            
            // Create the appointment/alarm
            val alarm = Appointment(
                id = 0,
                title = title,
                description = description,
                location = "",
                startTime = startTime,
                endTime = endTime,
                reminderMinutesBefore = 5,
                category = AppointmentCategory.OTHER,
                eventType = EventType.ALARM,
                medicationId = medication.id,
                isRecurring = recurringPattern != null,
                recurringPattern = recurringPattern,
                color = "#4CAF50", // Green for medication alarms
                createdAt = now
            )
            
            appointmentRepository.insertAppointment(alarm)
        } catch (e: Exception) {
            // Log error but don't fail the schedule creation
            println("Error creating alarm for schedule: ${e.message}")
        }
    }
    
    private suspend fun deleteAlarmsForMedication(medicationId: Long) {
        try {
            // Get all appointments that are alarms linked to this medication
            val allAppointments = appointmentRepository.getAllAppointmentsSync()
            val medicationAlarms = allAppointments.filter {
                it.medicationId == medicationId && it.eventType == EventType.ALARM
            }
            
            // Delete each alarm
            medicationAlarms.forEach { alarm ->
                appointmentRepository.deleteAppointment(alarm)
            }
        } catch (e: Exception) {
            println("Error deleting alarms for medication: ${e.message}")
        }
    }
    
    fun toggleSchedule(scheduleId: Long, enabled: Boolean) {
        viewModelScope.launch {
            try {
                scheduleRepository.setScheduleEnabled(scheduleId, enabled)
                if (enabled) {
                    scheduleRepository.getScheduleById(scheduleId)?.let { schedule ->
                        alarmScheduler.scheduleAlarm(schedule)
                    }
                } else {
                    alarmScheduler.cancelAlarm(scheduleId)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error toggling schedule")
            }
        }
    }
    
    fun searchMedications(query: String): Flow<List<Medication>> {
        return medicationRepository.searchMedications(query)
    }
    
    fun getMedicationFlow(medicationId: Long): Flow<Medication?> {
        return medicationRepository.getMedicationByIdFlow(medicationId)
    }
    
    fun getMedicationLogs(medicationId: Long) = medicationLogRepository.getLogsForMedication(medicationId)
    
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
