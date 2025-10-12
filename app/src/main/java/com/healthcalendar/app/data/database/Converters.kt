package com.healthcalendar.app.data.database

import androidx.room.TypeConverter
import com.healthcalendar.app.data.database.entities.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class Converters {
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
    
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }
    
    @TypeConverter
    fun fromMedicationForm(value: MedicationForm): String {
        return value.name
    }
    
    @TypeConverter
    fun toMedicationForm(value: String): MedicationForm {
        return MedicationForm.valueOf(value)
    }
    
    @TypeConverter
    fun fromScheduleFrequency(value: ScheduleFrequency): String {
        return value.name
    }
    
    @TypeConverter
    fun toScheduleFrequency(value: String): ScheduleFrequency {
        return ScheduleFrequency.valueOf(value)
    }
    
    @TypeConverter
    fun fromMedicationStatus(value: MedicationStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toMedicationStatus(value: String): MedicationStatus {
        return MedicationStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromAppointmentCategory(value: AppointmentCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toAppointmentCategory(value: String): AppointmentCategory {
        return AppointmentCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromEventType(value: EventType): String {
        return value.name
    }
    
    @TypeConverter
    fun toEventType(value: String): EventType {
        return EventType.valueOf(value)
    }
    
    @TypeConverter
    fun fromRecurringPattern(value: RecurringPattern?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toRecurringPattern(value: String?): RecurringPattern? {
        return value?.let { RecurringPattern.valueOf(it) }
    }
    
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isEmpty()) emptyList()
        else value.split(",").map { it.toInt() }
    }
    
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return value.joinToString(",")
    }
    
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isEmpty()) emptyList()
        else value.split(",").mapNotNull { it.toLongOrNull() }
    }
    
    @TypeConverter
    fun fromDocumentType(value: com.healthcalendar.app.data.database.entities.DocumentType): String {
        return value.name
    }
    
    @TypeConverter
    fun toDocumentType(value: String): com.healthcalendar.app.data.database.entities.DocumentType {
        return com.healthcalendar.app.data.database.entities.DocumentType.valueOf(value)
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString("|||")
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList()
        else value.split("|||")
    }
    
    @TypeConverter
    fun fromNoteCategory(value: NoteCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toNoteCategory(value: String): NoteCategory {
        return NoteCategory.valueOf(value)
    }
}
