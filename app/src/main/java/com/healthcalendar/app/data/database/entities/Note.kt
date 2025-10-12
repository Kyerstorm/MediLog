package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: NoteCategory = NoteCategory.GENERAL,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isPinned: Boolean = false
)

enum class NoteCategory {
    GENERAL,
    DOCTOR_QUESTIONS,
    SYMPTOMS,
    MEDICATION_NOTES,
    APPOINTMENT_NOTES,
    OTHER
}
