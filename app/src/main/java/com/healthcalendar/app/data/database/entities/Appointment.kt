package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "appointments",
    indices = [
        androidx.room.Index("startTime"),
        androidx.room.Index("endTime"),
        androidx.room.Index("eventType"),
        androidx.room.Index("category"),
        androidx.room.Index(value = ["startTime", "eventType"]),
        androidx.room.Index(value = ["eventType", "isRecurring"])
    ]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val location: String = "",
    val phoneNumber: String = "", // Phone number for calling (e.g., doctor's office)
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean = false, // All-day event flag
    val reminderMinutesBefore: Int = 30,
    val reminderDaysBefore: Int = 0, // Reminder X days before (0 = on the day, 1 = day before, etc.)
    val category: AppointmentCategory,
    val eventType: EventType = EventType.APPOINTMENT, // Distinguish between appointment and alarm
    val medicationId: Long? = null, // Link to medication for medication alarms (deprecated - use medicationIds)
    val medicationIds: List<Long> = emptyList(), // Link to multiple medications for medication alarms
    val isRecurring: Boolean = false,
    val recurringPattern: RecurringPattern? = null,
    val color: String = "#2196F3", // Hex color for calendar display
    val createdAt: LocalDateTime
)

enum class AppointmentCategory {
    DOCTOR,
    DENTIST,
    THERAPY,
    LAB_TEST,
    SURGERY,
    CHECKUP,
    VACCINATION,
    OTHER
}

enum class RecurringPattern {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    YEARLY
}

enum class EventType {
    APPOINTMENT,  // Calendar appointments (doctor visits, etc.)
    ALARM         // Simple time-based alarms/reminders
}
