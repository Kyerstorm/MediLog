package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.MedicationLog
import com.healthcalendar.app.data.database.entities.MedicationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface MedicationLogDao {
    
    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY scheduledTime DESC")
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>>
    
    @Query("SELECT * FROM medication_logs ORDER BY scheduledTime DESC")
    suspend fun getAllLogs(): List<MedicationLog>
    
    @Query("SELECT * FROM medication_logs WHERE scheduledTime >= :startDate AND scheduledTime <= :endDate ORDER BY scheduledTime DESC")
    fun getLogsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE scheduledTime >= :startDate AND scheduledTime <= :endDate ORDER BY scheduledTime DESC LIMIT :limit")
    suspend fun getLogsBetweenDatesSync(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int = 1000): List<MedicationLog>
    
    @Query("SELECT * FROM medication_logs WHERE status = :status ORDER BY scheduledTime DESC")
    fun getLogsByStatus(status: MedicationStatus): Flow<List<MedicationLog>>
    
    @Query("SELECT * FROM medication_logs WHERE id = :logId LIMIT 1")
    suspend fun getLogById(logId: Long): MedicationLog?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog): Long
    
    @Update
    suspend fun updateLog(log: MedicationLog)
    
    @Delete
    suspend fun deleteLog(log: MedicationLog)
    
    @Query("SELECT COUNT(*) FROM medication_logs WHERE medicationId = :medicationId AND status = 'TAKEN' AND scheduledTime >= :startDate")
    suspend fun getAdherenceCount(medicationId: Long, startDate: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM medication_logs WHERE medicationId = :medicationId AND status = 'MISSED' AND scheduledTime >= :startDate")
    suspend fun getMissedCount(medicationId: Long, startDate: LocalDateTime): Int
}
