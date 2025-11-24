package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index("medicationId"),
        androidx.room.Index("scheduledTime"),
        androidx.room.Index("status"),
        androidx.room.Index(value = ["medicationId", "scheduledTime"])
    ]
)
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val scheduledTime: LocalDateTime,
    val takenTime: LocalDateTime?,
    val status: MedicationStatus,
    val notes: String = ""
)

enum class MedicationStatus {
    TAKEN,
    MISSED,
    SKIPPED,
    PENDING
}
