package com.healthcalendar.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "nhs_medication_bookmarks")
data class NHSMedicationBookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationName: String,
    val nhsUrl: String,
    val isFavorite: Boolean = false,
    val viewedAt: LocalDateTime? = null,
    val viewCount: Int = 0,
    val addedAt: LocalDateTime
)
