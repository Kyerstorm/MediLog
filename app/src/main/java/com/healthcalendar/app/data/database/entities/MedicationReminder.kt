package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Entity(
    tableName = "medication_reminders",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId"), Index("date"), Index("time")]
)
data class MedicationReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val date: LocalDate,
    val time: LocalTime,
    val dosage: String, // e.g., "2 tablets", "5ml"
    val instructions: String = "",
    val isRecurring: Boolean = false,
    val recurringDays: List<Int> = emptyList(), // 0=Sunday, 1=Monday, etc.
    val isTaken: Boolean = false,
    val takenAt: kotlinx.datetime.LocalDateTime? = null,
    val alarmEnabled: Boolean = true,
    val notes: String = ""
)
