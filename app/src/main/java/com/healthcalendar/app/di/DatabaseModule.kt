package com.healthcalendar.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.healthcalendar.app.data.database.HealthDatabase
import com.healthcalendar.app.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    // Migration from version 8 to 9: Added medicationIds List<Long> to Appointment
    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add medicationIds column with empty list as default
            database.execSQL("ALTER TABLE appointments ADD COLUMN medicationIds TEXT NOT NULL DEFAULT ''")
        }
    }
    
    // Migration from version 9 to 10: Added amount field to Medication
    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add amount column with default value of "1"
            database.execSQL("ALTER TABLE medications ADD COLUMN amount TEXT NOT NULL DEFAULT '1'")
        }
    }
    
    // Migration from version 10 to 11: Added app_settings table
    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create app_settings table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_settings (
                    id INTEGER NOT NULL PRIMARY KEY,
                    selectedTheme TEXT NOT NULL DEFAULT 'DEFAULT',
                    isDarkMode INTEGER NOT NULL DEFAULT 0,
                    useDynamicColors INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            // Insert default settings
            database.execSQL("INSERT INTO app_settings (id, selectedTheme, isDarkMode, useDynamicColors) VALUES (1, 'DEFAULT', 0, 0)")
        }
    }
    
    // Migration from version 11 to 12: Added notificationSoundUri to app_settings
    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add notificationSoundUri column
            database.execSQL("ALTER TABLE app_settings ADD COLUMN notificationSoundUri TEXT DEFAULT NULL")
        }
    }
    
    // Migration from version 12 to 13: Added recentLocations to app_settings
    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add recentLocations column with empty list as default
            database.execSQL("ALTER TABLE app_settings ADD COLUMN recentLocations TEXT NOT NULL DEFAULT ''")
        }
    }
    
    // Migration from version 13 to 14: Added isPinned to scanned_documents
    private val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add isPinned column with default false (0)
            database.execSQL("ALTER TABLE scanned_documents ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
        }
    }

    // Migration from version 14 to 15: Add custom theme fields to app_settings
    private val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add useCustomTheme and customThemeJson columns
            database.execSQL("ALTER TABLE app_settings ADD COLUMN useCustomTheme INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE app_settings ADD COLUMN customThemeJson TEXT DEFAULT NULL")
        }
    }
    
    @Provides
    @Singleton
    fun provideHealthDatabase(@ApplicationContext context: Context): HealthDatabase {
        return Room.databaseBuilder(
            context,
            HealthDatabase::class.java,
            "health_calendar_database"
        )
            .addMigrations(
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15
            )
            .fallbackToDestructiveMigration() // Only as last resort
            .build()
    }
    
    @Provides
    fun provideMedicationDao(database: HealthDatabase): MedicationDao {
        return database.medicationDao()
    }
    
    @Provides
    fun provideMedicationScheduleDao(database: HealthDatabase): MedicationScheduleDao {
        return database.medicationScheduleDao()
    }
    
    @Provides
    fun provideMedicationLogDao(database: HealthDatabase): MedicationLogDao {
        return database.medicationLogDao()
    }
    
    @Provides
    fun provideMedicationReminderDao(database: HealthDatabase): MedicationReminderDao {
        return database.medicationReminderDao()
    }
    
    @Provides
    fun provideAppointmentDao(database: HealthDatabase): AppointmentDao {
        return database.appointmentDao()
    }
    
    @Provides
    fun provideScannedDocumentDao(database: HealthDatabase): ScannedDocumentDao {
        return database.scannedDocumentDao()
    }
    
    @Provides
    fun provideNHSMedicationBookmarkDao(database: HealthDatabase): NHSMedicationBookmarkDao {
        return database.nhsMedicationBookmarkDao()
    }
    
    @Provides
    fun provideNoteDao(database: HealthDatabase): NoteDao {
        return database.noteDao()
    }
    
    @Provides
    fun provideAppSettingsDao(database: HealthDatabase): AppSettingsDao {
        return database.appSettingsDao()
    }
}
