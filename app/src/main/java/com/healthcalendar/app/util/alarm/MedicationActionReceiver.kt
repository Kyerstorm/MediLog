package com.healthcalendar.app.util.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.healthcalendar.app.data.database.entities.MedicationStatus
import com.healthcalendar.app.data.repository.MedicationLogRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class MedicationActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var medicationLogRepository: MedicationLogRepository
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicationActionReceiver", "Action received: ${intent.action}")
        
        val medicationId = intent.getLongExtra("MEDICATION_ID", -1L)
        val logId = intent.getLongExtra("LOG_ID", -1L)
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
        
        if (medicationId == -1L) {
            Log.e("MedicationActionReceiver", "Invalid medication ID")
            return
        }
        
        when (intent.action) {
            ACTION_TAKEN -> {
                Log.d("MedicationActionReceiver", "Medication taken: ID=$medicationId")
                markMedicationTaken(context, medicationId, logId, notificationId)
            }
            ACTION_SKIPPED -> {
                Log.d("MedicationActionReceiver", "Medication skipped: ID=$medicationId")
                markMedicationSkipped(context, medicationId, logId, notificationId)
            }
        }
    }
    
    private fun markMedicationTaken(context: Context, medicationId: Long, logId: Long, notificationId: Int) {
        scope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                
                if (logId != -1L) {
                    // Update existing log
                    val log = medicationLogRepository.getLogById(logId)
                    if (log != null) {
                        medicationLogRepository.updateLog(
                            log.copy(
                                status = MedicationStatus.TAKEN,
                                takenTime = now
                            )
                        )
                    }
                } else {
                    // Create new log
                    val log = com.healthcalendar.app.data.database.entities.MedicationLog(
                        medicationId = medicationId,
                        scheduledTime = now,
                        takenTime = now,
                        status = MedicationStatus.TAKEN
                    )
                    medicationLogRepository.insertLog(log)
                }
                
                // Dismiss notification
                dismissNotification(context, notificationId)
                
                Log.d("MedicationActionReceiver", "✓ Medication marked as taken")
            } catch (e: Exception) {
                Log.e("MedicationActionReceiver", "Error marking medication as taken", e)
            }
        }
    }
    
    private fun markMedicationSkipped(context: Context, medicationId: Long, logId: Long, notificationId: Int) {
        scope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                
                if (logId != -1L) {
                    // Update existing log
                    val log = medicationLogRepository.getLogById(logId)
                    if (log != null) {
                        medicationLogRepository.updateLog(
                            log.copy(
                                status = MedicationStatus.SKIPPED,
                                notes = "Skipped by user"
                            )
                        )
                    }
                } else {
                    // Create new log
                    val log = com.healthcalendar.app.data.database.entities.MedicationLog(
                        medicationId = medicationId,
                        scheduledTime = now,
                        takenTime = null,
                        status = MedicationStatus.SKIPPED,
                        notes = "Skipped by user"
                    )
                    medicationLogRepository.insertLog(log)
                }
                
                // Dismiss notification
                dismissNotification(context, notificationId)
                
                Log.d("MedicationActionReceiver", "✓ Medication marked as skipped")
            } catch (e: Exception) {
                Log.e("MedicationActionReceiver", "Error marking medication as skipped", e)
            }
        }
    }
    
    private fun dismissNotification(context: Context, notificationId: Int) {
        if (notificationId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            Log.d("MedicationActionReceiver", "Notification dismissed: ID=$notificationId")
        }
    }
    
    companion object {
        const val ACTION_TAKEN = "com.healthcalendar.app.ACTION_MEDICATION_TAKEN"
        const val ACTION_SKIPPED = "com.healthcalendar.app.ACTION_MEDICATION_SKIPPED"
    }
}
