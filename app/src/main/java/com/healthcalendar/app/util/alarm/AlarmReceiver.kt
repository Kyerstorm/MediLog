package com.healthcalendar.app.util.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.healthcalendar.app.MainActivity
import com.healthcalendar.app.R
import com.healthcalendar.app.data.repository.AppSettingsRepository
import com.healthcalendar.app.data.repository.MedicationLogRepository
import com.healthcalendar.app.data.repository.MedicationRepository
import com.healthcalendar.app.data.database.entities.MedicationLog
import com.healthcalendar.app.data.database.entities.MedicationStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var medicationRepository: MedicationRepository
    
    @Inject
    lateinit var medicationLogRepository: MedicationLogRepository
    
    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("AlarmReceiver", "onReceive called!")
        val type = intent.getStringExtra("TYPE") ?: "MEDICATION"
        android.util.Log.d("AlarmReceiver", "Alarm type: $type")
        
        when (type) {
            "APPOINTMENT" -> handleAppointmentReminder(context, intent)
            else -> handleMedicationReminder(context, intent)
        }
    }
    
    private fun handleMedicationReminder(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra("MEDICATION_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "Medication"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""
        
        if (medicationId != -1L) {
            // Create notification
            showMedicationNotification(context, medicationId, medicationName, dosage)
            
            // Log the medication reminder
            scope.launch {
                try {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val log = MedicationLog(
                        medicationId = medicationId,
                        scheduledTime = now,
                        takenTime = null,
                        status = MedicationStatus.PENDING
                    )
                    medicationLogRepository.insertLog(log)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun handleAppointmentReminder(context: Context, intent: Intent) {
        val appointmentId = intent.getLongExtra("APPOINTMENT_ID", -1)
        val title = intent.getStringExtra("APPOINTMENT_TITLE") ?: "Appointment"
        val location = intent.getStringExtra("APPOINTMENT_LOCATION") ?: ""
        val time = intent.getStringExtra("APPOINTMENT_TIME") ?: ""
        val eventType = intent.getStringExtra("EVENT_TYPE") ?: "APPOINTMENT" // Get event type
        
        Log.d("AlarmReceiver", "Handling appointment reminder: $title at $time (type: $eventType)")
        
        if (appointmentId != -1L) {
            showAppointmentNotification(context, appointmentId, title, location, time, eventType)
        } else {
            Log.w("AlarmReceiver", "Invalid appointment ID: $appointmentId")
        }
    }
    
    private fun showMedicationNotification(
        context: Context,
        medicationId: Long,
        medicationName: String,
        dosage: String
    ) {
        android.util.Log.d("AlarmReceiver", "Showing medication notification for: $medicationName")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Get custom sound URI or use default
        val soundUri = runBlocking {
            val settings = appSettingsRepository.getSettingsOnce()
            settings.notificationSoundUri?.let { Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medication reminders"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("MEDICATION_ID", medicationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create action intents for "Taken" and "Skip"
        val notificationId = medicationId.toInt()
        
        val takenIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_TAKEN
            putExtra("MEDICATION_ID", medicationId)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1, // Unique request code
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val skippedIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_SKIPPED
            putExtra("MEDICATION_ID", medicationId)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val skippedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2, // Unique request code
            skippedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification with action buttons
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication_notification)
            .setContentTitle("Time to take your medication")
            .setContentText("$medicationName ${if (dosage.isNotEmpty()) "- $dosage" else ""}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false) // Don't auto-cancel, let user choose action
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_input_add,  // Use Android system icon
                "Taken",
                takenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,  // Use Android system icon
                "Skip",
                skippedPendingIntent
            )
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(soundUri) // Use custom or default sound
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setFullScreenIntent(pendingIntent, true) // Show on lock screen
            .build()
        
        
        notificationManager.notify(notificationId, notification)
        android.util.Log.d("AlarmReceiver", "Notification shown with ID: $notificationId with action buttons")
    }
    
    private fun showAppointmentNotification(
        context: Context,
        appointmentId: Long,
        title: String,
        location: String,
        time: String,
        eventType: String = "APPOINTMENT"
    ) {
        Log.d("AlarmReceiver", "Showing ${if (eventType == "ALARM") "alarm" else "appointment"} notification for: $title")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Get custom sound URI for ALARM types, or use default for appointments
        val soundUri = if (eventType == "ALARM") {
            runBlocking {
                val settings = appSettingsRepository.getSettingsOnce()
                settings.notificationSoundUri?.let { Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(if (eventType == "ALARM") AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                .build()
            
            val channel = NotificationChannel(
                APPOINTMENT_CHANNEL_ID,
                if (eventType == "ALARM") "Alarms" else "Appointment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = if (eventType == "ALARM") "Alarm notifications" else "Notifications for appointment reminders"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("APPOINTMENT_ID", appointmentId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.toInt() + 100000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notificationTitle = if (eventType == "ALARM") "⏰ Alarm" else "Upcoming Appointment"
        val notificationText = if (eventType == "ALARM") {
            title
        } else {
            "$title ${if (location.isNotEmpty()) "at $location" else ""}"
        }
        
        val notification = NotificationCompat.Builder(context, APPOINTMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_appointment_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$title\n${if (location.isNotEmpty()) "Location: $location\n" else ""}Time: $time"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (eventType == "ALARM") NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri) // Use the sound URI we determined earlier
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()
        
        notificationManager.notify(appointmentId.toInt() + 100000, notification)
        Log.d("AlarmReceiver", "Notification shown with ID: ${appointmentId.toInt() + 100000}")
    }
    
    companion object {
        private const val CHANNEL_ID = "medication_reminders"
        private const val APPOINTMENT_CHANNEL_ID = "appointment_reminders"
    }
}

