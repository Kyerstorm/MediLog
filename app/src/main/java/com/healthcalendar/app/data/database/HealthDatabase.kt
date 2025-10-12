package com.healthcalendar.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthcalendar.app.data.database.dao.*
import com.healthcalendar.app.data.database.entities.*

@Database(
    entities = [
        Medication::class,
        MedicationSchedule::class,
        MedicationLog::class,
        MedicationReminder::class,
        Appointment::class,
        ScannedDocumentEntity::class,
        NHSMedicationBookmark::class,
        Note::class,
        AppSettings::class
    ],
    version = 15,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun medicationReminderDao(): MedicationReminderDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun scannedDocumentDao(): ScannedDocumentDao
    abstract fun nhsMedicationBookmarkDao(): NHSMedicationBookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun appSettingsDao(): AppSettingsDao
}
