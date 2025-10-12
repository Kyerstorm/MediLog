package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "scanned_documents")
data class ScannedDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val fileUri: String,
    val mimeType: String = "", // e.g., "application/pdf", "image/jpeg", "application/msword"
    val fileSize: Long = 0, // Size in bytes
    val documentType: DocumentType,
    val medicationId: Long? = null, // Optional link to medication
    val appointmentId: Long? = null, // Optional link to appointment
    val scannedAt: LocalDateTime,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false // Pin to top of list
)

enum class DocumentType {
    PRESCRIPTION,
    LAB_REPORT,
    MEDICAL_RECORD,
    MEDICATION_INFO,
    INSURANCE,
    INVOICE,
    PDF_DOCUMENT,      // General PDF documents
    WORD_DOCUMENT,     // Word documents (.doc, .docx)
    SCANNED_IMAGE,     // Images from ML Kit scanner
    OTHER
}
