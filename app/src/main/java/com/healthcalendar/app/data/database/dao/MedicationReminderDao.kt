package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.MedicationReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Dao
interface MedicationReminderDao {
    
    @Query("SELECT * FROM medication_reminders ORDER BY date, time")
    fun getAllReminders(): Flow<List<MedicationReminder>>
    
    @Query("SELECT * FROM medication_reminders WHERE date = :date ORDER BY time")
    fun getRemindersForDate(date: LocalDate): Flow<List<MedicationReminder>>
    
    @Query("SELECT * FROM medication_reminders WHERE date = :date AND time = :time ORDER BY medicationId")
    fun getRemindersForDateTime(date: LocalDate, time: LocalTime): Flow<List<MedicationReminder>>
    
    @Query("SELECT * FROM medication_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): MedicationReminder?
    
    @Query("SELECT * FROM medication_reminders WHERE medicationId = :medicationId ORDER BY date, time")
    fun getRemindersForMedication(medicationId: Long): Flow<List<MedicationReminder>>
    
    @Query("SELECT * FROM medication_reminders WHERE date >= :startDate AND date <= :endDate ORDER BY date, time")
    fun getRemindersBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MedicationReminder>>
    
    @Query("SELECT * FROM medication_reminders WHERE isTaken = 0 AND date <= :date ORDER BY date, time")
    fun getMissedReminders(date: LocalDate): Flow<List<MedicationReminder>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminder): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<MedicationReminder>)
    
    @Update
    suspend fun updateReminder(reminder: MedicationReminder)
    
    @Delete
    suspend fun deleteReminder(reminder: MedicationReminder)
    
    @Query("DELETE FROM medication_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
    
    @Query("UPDATE medication_reminders SET isTaken = :isTaken, takenAt = :takenAt WHERE id = :id")
    suspend fun markReminderTaken(id: Long, isTaken: Boolean, takenAt: kotlinx.datetime.LocalDateTime?)
    
    @Query("SELECT DISTINCT date FROM medication_reminders WHERE date >= :startDate AND date <= :endDate")
    suspend fun getDatesWithReminders(startDate: LocalDate, endDate: LocalDate): List<LocalDate>
}
