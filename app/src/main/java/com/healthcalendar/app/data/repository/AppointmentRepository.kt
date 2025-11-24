package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.AppointmentDao
import com.healthcalendar.app.data.database.entities.Appointment
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    fun getAllAppointments(): Flow<List<Appointment>> =
        appointmentDao.getAllAppointments()

    suspend fun getAllAppointmentsSync(): List<Appointment> =
        appointmentDao.getAllAppointmentsSync()
    
    fun getAppointmentsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Appointment>> = 
        appointmentDao.getAppointmentsBetweenDates(startDate, endDate)
    
    suspend fun getAppointmentById(id: Long): Appointment? = 
        appointmentDao.getAppointmentById(id)
    
    fun getAppointmentByIdFlow(id: Long): Flow<Appointment?> = 
        appointmentDao.getAppointmentByIdFlow(id)
    
    suspend fun insertAppointment(appointment: Appointment): Long = 
        appointmentDao.insertAppointment(appointment)
    
    suspend fun updateAppointment(appointment: Appointment) = 
        appointmentDao.updateAppointment(appointment)
    
    suspend fun deleteAppointment(appointment: Appointment) = 
        appointmentDao.deleteAppointment(appointment)
    
    fun getUpcomingAppointments(date: LocalDateTime): Flow<List<Appointment>> = 
        appointmentDao.getUpcomingAppointments(date)
}
