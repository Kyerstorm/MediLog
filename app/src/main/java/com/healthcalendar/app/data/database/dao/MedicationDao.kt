package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun getAllMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Long): Medication?
    
    @Query("SELECT * FROM medications WHERE id = :medicationId")
    fun getMedicationByIdFlow(medicationId: Long): Flow<Medication?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long
    
    @Update
    suspend fun updateMedication(medication: Medication)
    
    @Delete
    suspend fun deleteMedication(medication: Medication)
    
    @Query("UPDATE medications SET isActive = 0 WHERE id = :medicationId")
    suspend fun deactivateMedication(medicationId: Long)
    
    @Query("SELECT * FROM medications WHERE name LIKE '%' || :query || '%' AND isActive = 1")
    fun searchMedications(query: String): Flow<List<Medication>>
}
