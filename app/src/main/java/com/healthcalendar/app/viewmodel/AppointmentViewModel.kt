package com.healthcalendar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.AppointmentCategory
import com.healthcalendar.app.data.database.entities.EventType
import com.healthcalendar.app.data.database.entities.RecurringPattern
import com.healthcalendar.app.data.database.entities.MedicationSchedule
import com.healthcalendar.app.data.database.entities.ScheduleFrequency
import com.healthcalendar.app.data.repository.AppointmentRepository
import com.healthcalendar.app.data.repository.AppSettingsRepository
import com.healthcalendar.app.data.repository.MedicationScheduleRepository
import com.healthcalendar.app.util.alarm.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val medicationScheduleRepository: MedicationScheduleRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    
    val appointments: StateFlow<List<Appointment>> = appointmentRepository.getAllAppointments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sorted alarms for AlarmsScreen - filtering and sorting done in background thread
    val sortedAlarms: StateFlow<List<Appointment>> = appointmentRepository.getAllAppointments()
        .map { allAppointments ->
            allAppointments
                .filter { it.eventType == EventType.ALARM }
                .sortedWith(compareBy({ it.startTime.date }, { it.startTime.time }))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val appSettings = appSettingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    private val _selectedAppointment = MutableStateFlow<Appointment?>(null)
    val selectedAppointment: StateFlow<Appointment?> = _selectedAppointment.asStateFlow()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun loadAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val appointment = appointmentRepository.getAppointmentById(appointmentId)
                _selectedAppointment.value = appointment
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun getAppointmentsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Appointment>> {
        return appointmentRepository.getAppointmentsBetweenDates(startDate, endDate)
    }
    
    fun getAppointmentsForDate(date: kotlinx.datetime.LocalDate): Flow<List<Appointment>> {
        val startOfDay = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0, 0)
        val endOfDay = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 23, 59, 59)
        return appointmentRepository.getAppointmentsBetweenDates(startOfDay, endOfDay)
    }
    
    fun getUpcomingAppointments(): Flow<List<Appointment>> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return appointmentRepository.getUpcomingAppointments(now)
    }
    
    fun getAppointmentById(id: Long): Flow<Appointment?> {
        return appointmentRepository.getAppointmentByIdFlow(id)
    }
    
    fun saveAppointment(
        id: Long? = null,
        title: String,
        description: String,
        location: String,
        phoneNumber: String = "",
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        isAllDay: Boolean = false,
        reminderMinutesBefore: Int,
        reminderDaysBefore: Int = 0,
        category: AppointmentCategory,
        eventType: EventType = EventType.APPOINTMENT,
        medicationId: Long? = null,
        medicationIds: List<Long> = emptyList(),
        isRecurring: Boolean,
        recurringPattern: RecurringPattern?,
        color: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val appointment = Appointment(
                    id = id ?: 0,
                    title = title,
                    description = description,
                    location = location,
                    phoneNumber = phoneNumber,
                    startTime = startTime,
                    endTime = endTime,
                    isAllDay = isAllDay,
                    reminderMinutesBefore = reminderMinutesBefore,
                    reminderDaysBefore = reminderDaysBefore,
                    category = category,
                    eventType = eventType,
                    medicationId = medicationId,
                    medicationIds = medicationIds,
                    isRecurring = isRecurring,
                    recurringPattern = recurringPattern,
                    color = color,
                    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )
                
                val savedId = appointmentRepository.insertAppointment(appointment)
                
                // Save location to recent locations if not blank
                if (location.isNotBlank()) {
                    appSettingsRepository.addRecentLocation(location)
                }
                
                // Schedule reminder alarm
                val savedAppointment = appointment.copy(id = savedId)
                alarmScheduler.scheduleAppointmentReminder(savedAppointment)
                
                // If this is an ALARM with medications, create medication schedules
                if (eventType == EventType.ALARM && medicationIds.isNotEmpty()) {
                    createMedicationSchedules(
                        medicationIds = medicationIds,
                        alarmTime = startTime,
                        isRecurring = isRecurring,
                        recurringPattern = recurringPattern
                    )
                }
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error saving appointment")
            }
        }
    }
    
    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                alarmScheduler.cancelAppointmentReminder(appointment.id)
                appointmentRepository.deleteAppointment(appointment)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error deleting appointment")
            }
        }
    }
    
    // Simplified add appointment method
    fun addAppointment(
        title: String,
        description: String,
        location: String,
        phoneNumber: String = "",
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        isAllDay: Boolean = false,
        reminderDaysBefore: Int = 0,
        eventType: EventType = EventType.APPOINTMENT,
        medicationId: Long? = null,
        medicationIds: List<Long> = emptyList(),
        isRecurring: Boolean = false,
        recurringPattern: RecurringPattern? = null
    ) {
        saveAppointment(
            id = null,
            title = title,
            description = description,
            location = location,
            phoneNumber = phoneNumber,
            startTime = startTime,
            endTime = endTime,
            isAllDay = isAllDay,
            reminderMinutesBefore = 30,
            reminderDaysBefore = reminderDaysBefore,
            category = AppointmentCategory.OTHER,
            eventType = eventType,
            medicationId = medicationId,
            medicationIds = medicationIds,
            isRecurring = isRecurring,
            recurringPattern = recurringPattern,
            color = "#2196F3"
        )
    }
    
    /**
     * Creates medication schedules when an alarm is saved with linked medications.
     * This allows the alarm to automatically schedule medication reminders.
     */
    private suspend fun createMedicationSchedules(
        medicationIds: List<Long>,
        alarmTime: LocalDateTime,
        isRecurring: Boolean,
        recurringPattern: RecurringPattern?
    ) {
        val time = LocalTime(alarmTime.hour, alarmTime.minute)
        
        // Determine frequency based on recurring pattern
        val frequency = when {
            !isRecurring -> ScheduleFrequency.AS_NEEDED
            recurringPattern == RecurringPattern.DAILY -> ScheduleFrequency.DAILY
            recurringPattern == RecurringPattern.WEEKLY -> ScheduleFrequency.WEEKLY
            else -> ScheduleFrequency.CUSTOM
        }
        
        // Determine days of week for weekly recurring alarms
        val daysOfWeek = when (recurringPattern) {
            RecurringPattern.WEEKLY -> listOf(alarmTime.dayOfWeek.value) // ISO day of week (1=Monday, 7=Sunday)
            RecurringPattern.DAILY -> (1..7).toList() // All days
            else -> emptyList()
        }
        
        // Create a schedule for each linked medication
        medicationIds.forEach { medicationId ->
            val schedule = MedicationSchedule(
                medicationId = medicationId,
                time = time,
                frequency = frequency,
                daysOfWeek = daysOfWeek,
                isEnabled = true,
                lastAlarmSet = Clock.System.now().toEpochMilliseconds()
            )
            
            val scheduleId = medicationScheduleRepository.insertSchedule(schedule)
            
            // Schedule the alarm through AlarmScheduler
            val savedSchedule = schedule.copy(id = scheduleId)
            alarmScheduler.scheduleAlarm(savedSchedule)
        }
    }
    
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
