package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.MedicationScheduleDao
import com.healthcalendar.app.data.database.entities.MedicationSchedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationScheduleRepository @Inject constructor(
    private val scheduleDao: MedicationScheduleDao
) {
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>> = 
        scheduleDao.getSchedulesForMedication(medicationId)
    
    fun getAllActiveSchedules(): Flow<List<MedicationSchedule>> = 
        scheduleDao.getAllActiveSchedules()
    
    suspend fun getScheduleById(id: Long): MedicationSchedule? = 
        scheduleDao.getScheduleById(id)
    
    suspend fun insertSchedule(schedule: MedicationSchedule): Long = 
        scheduleDao.insertSchedule(schedule)
    
    suspend fun updateSchedule(schedule: MedicationSchedule) = 
        scheduleDao.updateSchedule(schedule)
    
    suspend fun deleteSchedule(schedule: MedicationSchedule) = 
        scheduleDao.deleteSchedule(schedule)
    
    suspend fun setScheduleEnabled(scheduleId: Long, enabled: Boolean) = 
        scheduleDao.setScheduleEnabled(scheduleId, enabled)
    
    suspend fun deleteSchedulesForMedication(medicationId: Long) = 
        scheduleDao.deleteSchedulesForMedication(medicationId)
}
