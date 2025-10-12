package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.ScannedDocumentDao
import com.healthcalendar.app.data.database.entities.ScannedDocumentEntity
import com.healthcalendar.app.data.database.entities.DocumentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannedDocumentRepository @Inject constructor(
    private val scannedDocumentDao: ScannedDocumentDao
) {
    fun getAllDocuments(): Flow<List<ScannedDocumentEntity>> = 
        scannedDocumentDao.getAllDocuments()
    
    suspend fun getDocumentById(id: Long): ScannedDocumentEntity? = 
        scannedDocumentDao.getDocumentById(id)
    
    fun getDocumentByIdFlow(id: Long): Flow<ScannedDocumentEntity?> = 
        scannedDocumentDao.getDocumentByIdFlow(id)
    
    fun getDocumentsForMedication(medicationId: Long): Flow<List<ScannedDocumentEntity>> = 
        scannedDocumentDao.getDocumentsForMedication(medicationId)
    
    fun getDocumentsForAppointment(appointmentId: Long): Flow<List<ScannedDocumentEntity>> = 
        scannedDocumentDao.getDocumentsForAppointment(appointmentId)
    
    fun getDocumentsByType(type: DocumentType): Flow<List<ScannedDocumentEntity>> = 
        scannedDocumentDao.getDocumentsByType(type)
    
    fun searchDocuments(query: String): Flow<List<ScannedDocumentEntity>> = 
        scannedDocumentDao.searchDocuments(query)
    
    suspend fun insertDocument(document: ScannedDocumentEntity): Long = 
        scannedDocumentDao.insertDocument(document)
    
    suspend fun updateDocument(document: ScannedDocumentEntity) = 
        scannedDocumentDao.updateDocument(document)
    
    suspend fun deleteDocument(document: ScannedDocumentEntity) = 
        scannedDocumentDao.deleteDocument(document)
    
    suspend fun deleteDocumentsForMedication(medicationId: Long) = 
        scannedDocumentDao.deleteDocumentsForMedication(medicationId)
    
    suspend fun togglePin(documentId: Long, isPinned: Boolean) = 
        scannedDocumentDao.setPinned(documentId, isPinned)
}
