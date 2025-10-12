package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String, // Dosage per tablet/unit (e.g., "500" for 500mg tablets)
    val amount: String = "1", // Number of tablets/units to take (e.g., "2" for 2 tablets)
    val unit: String, // mg, ml, tablets, etc.
    val form: MedicationForm,
    val instructions: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val createdAt: LocalDateTime,
    val isActive: Boolean = true
)

enum class MedicationForm {
    TABLET,
    CAPSULE,
    LIQUID,
    INJECTION,
    INHALER,
    CREAM,
    DROPS,
    PATCH,
    OTHER
}
