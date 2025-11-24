package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.Appointment
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface AppointmentDao {
    
    @Query("SELECT * FROM appointments ORDER BY startTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments ORDER BY startTime ASC")
    suspend fun getAllAppointmentsSync(): List<Appointment>
    
    @Query("SELECT * FROM appointments WHERE startTime >= :startDate AND startTime <= :endDate ORDER BY startTime ASC")
    fun getAppointmentsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Appointment>>
    
    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Long): Appointment?
    
    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    fun getAppointmentByIdFlow(appointmentId: Long): Flow<Appointment?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long
    
    @Update
    suspend fun updateAppointment(appointment: Appointment)
    
    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
    
    @Query("SELECT * FROM appointments WHERE startTime >= :date ORDER BY startTime ASC LIMIT 5")
    fun getUpcomingAppointments(date: LocalDateTime): Flow<List<Appointment>>
}
