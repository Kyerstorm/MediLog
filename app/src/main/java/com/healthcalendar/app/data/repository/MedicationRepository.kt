package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.MedicationDao
import com.healthcalendar.app.data.database.dao.MedicationLogDao
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationLog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val medicationLogDao: MedicationLogDao
) {
    val medications: Flow<List<Medication>> = medicationDao.getAllMedications()
    
    fun getAllActiveMedications(): Flow<List<Medication>> = 
        medicationDao.getAllActiveMedications()
    
    fun getAllMedications(): Flow<List<Medication>> = 
        medicationDao.getAllMedications()
    
    suspend fun getMedicationById(id: Long): Medication? = 
        medicationDao.getMedicationById(id)
    
    fun getMedicationByIdFlow(id: Long): Flow<Medication?> = 
        medicationDao.getMedicationByIdFlow(id)
    
    suspend fun insertMedication(medication: Medication): Long = 
        medicationDao.insertMedication(medication)
    
    suspend fun updateMedication(medication: Medication) = 
        medicationDao.updateMedication(medication)
    
    suspend fun deleteMedication(medication: Medication) = 
        medicationDao.deleteMedication(medication)
    
    suspend fun deactivateMedication(medicationId: Long) = 
        medicationDao.deactivateMedication(medicationId)
    
    fun searchMedications(query: String): Flow<List<Medication>> = 
        medicationDao.searchMedications(query)
    
    suspend fun getAllMedicationLogs(): List<MedicationLog> = 
        medicationLogDao.getAllLogs()
}
