package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.MedicationSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationScheduleDao {
    
    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId AND isEnabled = 1")
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>>
    
    @Query("SELECT * FROM medication_schedules WHERE isEnabled = 1")
    fun getAllActiveSchedules(): Flow<List<MedicationSchedule>>
    
    @Query("SELECT * FROM medication_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): MedicationSchedule?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationSchedule): Long
    
    @Update
    suspend fun updateSchedule(schedule: MedicationSchedule)
    
    @Delete
    suspend fun deleteSchedule(schedule: MedicationSchedule)
    
    @Query("UPDATE medication_schedules SET isEnabled = :enabled WHERE id = :scheduleId")
    suspend fun setScheduleEnabled(scheduleId: Long, enabled: Boolean)
    
    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)
}
