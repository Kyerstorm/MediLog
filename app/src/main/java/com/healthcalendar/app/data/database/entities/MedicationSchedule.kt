package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalTime

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val time: LocalTime,
    val frequency: ScheduleFrequency,
    val daysOfWeek: List<Int> = emptyList(), // 1-7 for Monday-Sunday
    val isEnabled: Boolean = true,
    val lastAlarmSet: Long = 0L // Timestamp when alarm was last set
)

enum class ScheduleFrequency {
    DAILY,
    WEEKLY,
    AS_NEEDED,
    CUSTOM
}
