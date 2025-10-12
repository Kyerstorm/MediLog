package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.MedicationLogDao
import com.healthcalendar.app.data.database.entities.MedicationLog
import com.healthcalendar.app.data.database.entities.MedicationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationLogRepository @Inject constructor(
    private val logDao: MedicationLogDao
) {
    suspend fun getAllLogs(): List<MedicationLog> = 
        logDao.getAllLogs()
    
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>> = 
        logDao.getLogsForMedication(medicationId)
    
    fun getLogsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<MedicationLog>> = 
        logDao.getLogsBetweenDates(startDate, endDate)
    
    fun getLogsByStatus(status: MedicationStatus): Flow<List<MedicationLog>> = 
        logDao.getLogsByStatus(status)
    
    suspend fun getLogById(logId: Long): MedicationLog? =
        logDao.getLogById(logId)
    
    suspend fun insertLog(log: MedicationLog): Long = 
        logDao.insertLog(log)
    
    suspend fun updateLog(log: MedicationLog) = 
        logDao.updateLog(log)
    
    suspend fun deleteLog(log: MedicationLog) = 
        logDao.deleteLog(log)
    
    suspend fun getAdherenceCount(medicationId: Long, startDate: LocalDateTime): Int = 
        logDao.getAdherenceCount(medicationId, startDate)
    
    suspend fun getMissedCount(medicationId: Long, startDate: LocalDateTime): Int = 
        logDao.getMissedCount(medicationId, startDate)
}
