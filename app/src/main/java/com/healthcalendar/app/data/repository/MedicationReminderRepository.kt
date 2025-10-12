package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.MedicationReminderDao
import com.healthcalendar.app.data.database.entities.MedicationReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationReminderRepository @Inject constructor(
    private val reminderDao: MedicationReminderDao
) {
    
    fun getAllReminders(): Flow<List<MedicationReminder>> {
        return reminderDao.getAllReminders()
    }
    
    fun getRemindersForDate(date: LocalDate): Flow<List<MedicationReminder>> {
        return reminderDao.getRemindersForDate(date)
    }
    
    fun getRemindersForDateTime(date: LocalDate, time: LocalTime): Flow<List<MedicationReminder>> {
        return reminderDao.getRemindersForDateTime(date, time)
    }
    
    fun getMissedReminders(beforeDate: LocalDate): Flow<List<MedicationReminder>> {
        return reminderDao.getMissedReminders(beforeDate)
    }
    
    suspend fun getDatesWithReminders(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        return reminderDao.getDatesWithReminders(startDate, endDate)
    }
    
    suspend fun insertReminder(reminder: MedicationReminder): Long {
        return reminderDao.insertReminder(reminder)
    }
    
    suspend fun updateReminder(reminder: MedicationReminder) {
        reminderDao.updateReminder(reminder)
    }
    
    suspend fun deleteReminder(reminder: MedicationReminder) {
        reminderDao.deleteReminder(reminder)
    }
    
    suspend fun markReminderTaken(id: Long, isTaken: Boolean, takenAt: LocalDateTime?) {
        reminderDao.markReminderTaken(id, isTaken, takenAt)
    }
}
