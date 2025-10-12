package com.healthcalendar.app.util.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling alarms")
            
            // Reschedule all medication alarms
            scope.launch {
                try {
                    alarmScheduler.rescheduleAllAlarms()
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms", e)
                }
            }
        }
    }
}
