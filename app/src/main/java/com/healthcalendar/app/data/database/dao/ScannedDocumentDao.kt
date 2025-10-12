package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.ScannedDocumentEntity
import com.healthcalendar.app.data.database.entities.DocumentType
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedDocumentDao {
    
    @Query("SELECT * FROM scanned_documents ORDER BY isPinned DESC, scannedAt DESC")
    fun getAllDocuments(): Flow<List<ScannedDocumentEntity>>
    
    @Query("SELECT * FROM scanned_documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: Long): ScannedDocumentEntity?
    
    @Query("SELECT * FROM scanned_documents WHERE id = :documentId")
    fun getDocumentByIdFlow(documentId: Long): Flow<ScannedDocumentEntity?>
    
    @Query("SELECT * FROM scanned_documents WHERE medicationId = :medicationId ORDER BY isPinned DESC, scannedAt DESC")
    fun getDocumentsForMedication(medicationId: Long): Flow<List<ScannedDocumentEntity>>
    
    @Query("SELECT * FROM scanned_documents WHERE appointmentId = :appointmentId ORDER BY isPinned DESC, scannedAt DESC")
    fun getDocumentsForAppointment(appointmentId: Long): Flow<List<ScannedDocumentEntity>>
    
    @Query("SELECT * FROM scanned_documents WHERE documentType = :type ORDER BY isPinned DESC, scannedAt DESC")
    fun getDocumentsByType(type: DocumentType): Flow<List<ScannedDocumentEntity>>
    
    @Query("SELECT * FROM scanned_documents WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY isPinned DESC, scannedAt DESC")
    fun searchDocuments(query: String): Flow<List<ScannedDocumentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: ScannedDocumentEntity): Long
    
    @Update
    suspend fun updateDocument(document: ScannedDocumentEntity)
    
    @Delete
    suspend fun deleteDocument(document: ScannedDocumentEntity)
    
    @Query("UPDATE scanned_documents SET isPinned = :isPinned WHERE id = :documentId")
    suspend fun setPinned(documentId: Long, isPinned: Boolean)
    
    @Query("DELETE FROM scanned_documents WHERE medicationId = :medicationId")
    suspend fun deleteDocumentsForMedication(medicationId: Long)
}
