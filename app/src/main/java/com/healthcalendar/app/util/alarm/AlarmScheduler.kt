package com.healthcalendar.app.util.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.EventType
import com.healthcalendar.app.data.database.entities.MedicationSchedule
import com.healthcalendar.app.data.database.entities.ScheduleFrequency
import com.healthcalendar.app.data.repository.MedicationRepository
import com.healthcalendar.app.data.repository.MedicationScheduleRepository
import com.healthcalendar.app.data.repository.AppointmentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: MedicationScheduleRepository,
    private val appointmentRepository: AppointmentRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    suspend fun scheduleAlarm(schedule: MedicationSchedule) {
        val medication = medicationRepository.getMedicationById(schedule.medicationId) ?: return
        
        // Calculate next alarm time
        val alarmTime = calculateNextAlarmTime(schedule)
        val alarmDate = Date(alarmTime)
        Log.d("AlarmScheduler", "Scheduling alarm for ${medication.name} at $alarmDate (timestamp: $alarmTime)")
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TYPE", "MEDICATION")
            putExtra("MEDICATION_ID", medication.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("DOSAGE", "${medication.amount} x ${medication.dosage} ${medication.unit}")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "✓ Scheduled exact alarm for ${medication.name} at $alarmDate")
                } else {
                    Log.w("AlarmScheduler", "✗ Cannot schedule exact alarms, permission denied")
                    Log.w("AlarmScheduler", "User needs to grant SCHEDULE_EXACT_ALARM permission in Settings")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
                Log.d("AlarmScheduler", "✓ Scheduled exact alarm for ${medication.name} at $alarmDate")
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "✗ Error scheduling alarm: ${e.message}", e)
        }
    }
    
    fun cancelAlarm(scheduleId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Cancelled alarm for schedule $scheduleId")
    }
    
    suspend fun rescheduleAllAlarms() {
        val schedules = scheduleRepository.getAllActiveSchedules().first()
        schedules.forEach { schedule ->
            scheduleAlarm(schedule)
        }
        Log.d("AlarmScheduler", "Rescheduled ${schedules.size} alarms")
    }
    
    private fun calculateNextAlarmTime(schedule: MedicationSchedule): Long {
        val now = Clock.System.now()
        val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timeZone).date
        
        // Combine today's date with the scheduled time
        val scheduledDateTime = LocalDateTime(
            today.year,
            today.month,
            today.dayOfMonth,
            schedule.time.hour,
            schedule.time.minute,
            schedule.time.second
        )
        
        var alarmDateTime = scheduledDateTime.toInstant(timeZone)
        
        // If the time has already passed today, schedule for tomorrow
        if (alarmDateTime <= now) {
            val tomorrow = today.plus(1, DateTimeUnit.DAY)
            val tomorrowDateTime = LocalDateTime(
                tomorrow.year,
                tomorrow.month,
                tomorrow.dayOfMonth,
                schedule.time.hour,
                schedule.time.minute,
                schedule.time.second
            )
            alarmDateTime = tomorrowDateTime.toInstant(timeZone)
        }
        
        // Handle weekly schedules
        if (schedule.frequency == ScheduleFrequency.WEEKLY && schedule.daysOfWeek.isNotEmpty()) {
            // Find the next day in the schedule
            var daysToAdd = 0
            var currentDay = today.dayOfWeek.value // 1 = Monday, 7 = Sunday
            
            // Look for the next scheduled day within the next 7 days
            for (i in 0..6) {
                val checkDay = (currentDay + i - 1) % 7 + 1
                if (schedule.daysOfWeek.contains(checkDay)) {
                    if (i == 0 && alarmDateTime > now) {
                        // Today is scheduled and time hasn't passed
                        daysToAdd = 0
                        break
                    } else if (i > 0) {
                        daysToAdd = i
                        break
                    }
                }
            }
            
            val nextDate = today.plus(daysToAdd, DateTimeUnit.DAY)
            val nextDateTime = LocalDateTime(
                nextDate.year,
                nextDate.month,
                nextDate.dayOfMonth,
                schedule.time.hour,
                schedule.time.minute,
                schedule.time.second
            )
            alarmDateTime = nextDateTime.toInstant(timeZone)
        }
        
        
        return alarmDateTime.toEpochMilliseconds()
    }
    
    // Appointment reminder scheduling
    suspend fun scheduleAppointmentReminder(appointment: Appointment) {
        val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
        var reminderTime = appointment.startTime.toInstant(timeZone)
        
        Log.d("AlarmScheduler", "Scheduling appointment reminder for: ${appointment.title}")
        Log.d("AlarmScheduler", "Appointment time: ${appointment.startTime}")
        Log.d("AlarmScheduler", "Event type: ${appointment.eventType}")
        Log.d("AlarmScheduler", "Days before: ${appointment.reminderDaysBefore}, Minutes before: ${appointment.reminderMinutesBefore}")
        
        // For ALARM type, schedule at exact time (not before)
        // For APPOINTMENT type, schedule reminder before the appointment
        if (appointment.eventType == EventType.APPOINTMENT) {
            // Subtract days-before reminder
            if (appointment.reminderDaysBefore > 0) {
                val daysInMillis = appointment.reminderDaysBefore * 24 * 60 * 60 * 1000L
                reminderTime = Instant.fromEpochMilliseconds(reminderTime.toEpochMilliseconds() - daysInMillis)
            }
            
            // Subtract minutes-before reminder  
            val minutesInMillis = appointment.reminderMinutesBefore * 60 * 1000L
            reminderTime = Instant.fromEpochMilliseconds(reminderTime.toEpochMilliseconds() - minutesInMillis)
        }
        // For ALARM type, reminderTime stays as startTime (exact time)
        
        val reminderDate = Date(reminderTime.toEpochMilliseconds())
        Log.d("AlarmScheduler", "Calculated reminder time: $reminderDate (timestamp: ${reminderTime.toEpochMilliseconds()})")
        
        // Only schedule if reminder is in the future
        if (reminderTime.toEpochMilliseconds() <= Clock.System.now().toEpochMilliseconds()) {
            Log.w("AlarmScheduler", "✗ Appointment reminder time has passed, skipping")
            return
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TYPE", "APPOINTMENT")
            putExtra("APPOINTMENT_ID", appointment.id)
            putExtra("APPOINTMENT_TITLE", appointment.title)
            putExtra("APPOINTMENT_LOCATION", appointment.location)
            putExtra("APPOINTMENT_TIME", appointment.startTime.toString())
            putExtra("EVENT_TYPE", appointment.eventType.name) // Pass event type (ALARM or APPOINTMENT)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.toInt() + 100000, // Offset to avoid collision with medication IDs
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime.toEpochMilliseconds(),
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "✓ Scheduled exact appointment alarm for ${appointment.title}")
                } else {
                    Log.w("AlarmScheduler", "✗ Cannot schedule exact alarms for appointment ${appointment.title}")
                    Log.w("AlarmScheduler", "User needs to grant SCHEDULE_EXACT_ALARM permission in Settings")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.toEpochMilliseconds(),
                    pendingIntent
                )
                Log.d("AlarmScheduler", "✓ Scheduled appointment alarm for ${appointment.title}")
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling appointment reminder", e)
        }
    }
    
    fun cancelAppointmentReminder(appointmentId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.toInt() + 100000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Cancelled appointment reminder for $appointmentId")
    }
    
    suspend fun rescheduleAllAppointmentReminders() {
        val now = Clock.System.now()
        val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
        val appointments = appointmentRepository.getAllAppointments().first()
        
        // Only reschedule future appointments
        appointments.filter { 
            it.startTime.toInstant(timeZone) > now 
        }.forEach { appointment ->
            scheduleAppointmentReminder(appointment)
        }
        Log.d("AlarmScheduler", "Rescheduled appointment reminders")
    }
}

