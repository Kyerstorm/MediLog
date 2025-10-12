package com.healthcalendar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.database.entities.MedicationReminder
import com.healthcalendar.app.data.repository.MedicationReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class MedicationReminderViewModel @Inject constructor(
    private val repository: MedicationReminderRepository
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate
    
    fun getAllReminders(): Flow<List<MedicationReminder>> {
        return repository.getAllReminders()
    }
    
    fun getRemindersForDate(date: LocalDate): Flow<List<MedicationReminder>> {
        return repository.getRemindersForDate(date)
    }
    
    fun getRemindersForDateTime(date: LocalDate, time: LocalTime): Flow<List<MedicationReminder>> {
        return repository.getRemindersForDateTime(date, time)
    }
    
    fun addReminder(
        medicationId: Long,
        date: LocalDate,
        time: LocalTime,
        dosage: String,
        instructions: String = "",
        isRecurring: Boolean = false,
        recurringDays: List<Int> = emptyList(),
        alarmEnabled: Boolean = true,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val reminder = MedicationReminder(
                medicationId = medicationId,
                date = date,
                time = time,
                dosage = dosage,
                instructions = instructions,
                isRecurring = isRecurring,
                recurringDays = recurringDays,
                alarmEnabled = alarmEnabled,
                notes = notes
            )
            repository.insertReminder(reminder)
        }
    }
    
    fun updateReminder(reminder: MedicationReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }
    
    fun deleteReminder(reminder: MedicationReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
    
    fun markReminderTaken(id: Long, isTaken: Boolean, takenAt: LocalDateTime?) {
        viewModelScope.launch {
            repository.markReminderTaken(id, isTaken, takenAt)
        }
    }
    
    fun getDatesWithReminders(startDate: LocalDate, endDate: LocalDate, onResult: (List<LocalDate>) -> Unit) {
        viewModelScope.launch {
            val dates = repository.getDatesWithReminders(startDate, endDate)
            onResult(dates)
        }
    }
    
    fun setSelectedDate(date: LocalDate?) {
        _selectedDate.value = date
    }
}
