package com.healthcalendar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.EventType
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationReminder
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.ui.util.getWindowSizeClass
import com.healthcalendar.app.ui.util.responsivePadding
import com.healthcalendar.app.ui.util.WindowSizeClass
import com.healthcalendar.app.viewmodel.AppointmentViewModel
import com.healthcalendar.app.viewmodel.MedicationViewModel
import com.healthcalendar.app.viewmodel.MedicationReminderViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    navController: NavController,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    medicationViewModel: MedicationViewModel = hiltViewModel(),
    medicationReminderViewModel: MedicationReminderViewModel = hiltViewModel()
) {
    // Use pre-sorted StateFlows from ViewModels for better performance
    val alarms by appointmentViewModel.sortedAlarms.collectAsState()
    val medicationRemindersWithAlarms by medicationReminderViewModel.sortedRemindersWithAlarms.collectAsState()

    // Compute unique medication IDs needed for display (optimized - no longer loads ALL medications)
    val neededMedicationIds = remember(alarms, medicationRemindersWithAlarms) {
        val ids = mutableSetOf<Long>()
        // Add medication IDs from alarms
        alarms.forEach { alarm ->
            alarm.medicationId?.let { ids.add(it) }
            ids.addAll(alarm.medicationIds)
        }
        // Add medication IDs from reminders
        medicationRemindersWithAlarms.forEach { reminder ->
            ids.add(reminder.medicationId)
        }
        ids.toList()
    }

    // Fetch only the medications we actually need (not all 100+)
    var medicationMap by remember { mutableStateOf<Map<Long, Medication>>(emptyMap()) }
    LaunchedEffect(neededMedicationIds) {
        if (neededMedicationIds.isNotEmpty()) {
            val meds = medicationViewModel.getMedicationsByIds(neededMedicationIds)
            medicationMap = meds.associateBy { it.id }
        } else {
            medicationMap = emptyMap()
        }
    }
    
    // Combined count for header
    val totalAlarmCount = alarms.size + medicationRemindersWithAlarms.size
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddAlarm.route) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add alarm")
            }
        }
    ) { paddingValues ->
        val padding = responsivePadding()
        
        if (totalAlarmCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No alarms set",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap the + button to add an alarm or set medication reminders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "$totalAlarmCount alarm${if (totalAlarmCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Show medication reminders first (they're typically more frequent)
                if (medicationRemindersWithAlarms.isNotEmpty()) {
                    item {
                        Text(
                            text = "Medication Reminders",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(medicationRemindersWithAlarms) { reminder ->
                        val medication = medicationMap[reminder.medicationId]
                        MedicationReminderCard(
                            reminder = reminder,
                            medication = medication,
                            onMarkTaken = {
                                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                medicationReminderViewModel.markReminderTaken(
                                    id = reminder.id,
                                    isTaken = !reminder.isTaken,
                                    takenAt = if (!reminder.isTaken) now else null
                                )
                            },
                            onDelete = {
                                medicationReminderViewModel.deleteReminder(reminder)
                            }
                        )
                    }
                }
                
                // Show regular alarms
                if (alarms.isNotEmpty()) {
                    item {
                        Text(
                            text = "Custom Alarms",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(alarms) { alarm ->
                        val linkedMedications = alarm.medicationIds.mapNotNull { medicationMap[it] }
                        // Backward compatibility: if medicationIds is empty but medicationId is set, use it
                        val medications = if (linkedMedications.isEmpty() && alarm.medicationId != null) {
                            listOfNotNull(medicationMap[alarm.medicationId])
                        } else {
                            linkedMedications
                        }
                        
                        AlarmCard(
                            alarm = alarm,
                            medications = medications,
                            onClick = {
                                navController.navigate(Screen.EditAppointment.createRoute(alarm.id))
                            },
                            onDelete = {
                                appointmentViewModel.deleteAppointment(alarm)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmCard(
    alarm: Appointment,
    medications: List<Medication>,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(alarm.color)).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", alarm.startTime.hour, alarm.startTime.minute),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(android.graphics.Color.parseColor(alarm.color))
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Show medications if linked
                if (medications.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (medications.size == 1) {
                                val med = medications[0]
                                "${med.name} - ${med.dosage} ${med.unit}"
                            } else {
                                "${medications.size} medications: ${medications.joinToString(", ") { it.name }}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${alarm.startTime.date.dayOfMonth}/${alarm.startTime.date.monthNumber}/${alarm.startTime.date.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (alarm.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alarm.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                if (alarm.isRecurring) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (alarm.recurringPattern) {
                                com.healthcalendar.app.data.database.entities.RecurringPattern.DAILY -> "Repeats daily"
                                com.healthcalendar.app.data.database.entities.RecurringPattern.WEEKLY -> "Repeats weekly"
                                com.healthcalendar.app.data.database.entities.RecurringPattern.BIWEEKLY -> "Repeats every 2 weeks"
                                com.healthcalendar.app.data.database.entities.RecurringPattern.MONTHLY -> "Repeats monthly"
                                com.healthcalendar.app.data.database.entities.RecurringPattern.YEARLY -> "Repeats yearly"
                                null -> "Recurring"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "View details"
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Alarm?") },
            text = { Text("Are you sure you want to delete \"${alarm.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationReminderCard(
    reminder: MedicationReminder,
    medication: Medication?,
    onMarkTaken: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isTaken) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (reminder.isTaken) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", reminder.time.hour, reminder.time.minute),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (reminder.isTaken) 
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                    if (reminder.isTaken) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Taken",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                // Medication name and dosage
                if (medication != null) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (reminder.isTaken) 
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${reminder.dosage} ${medication.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "Medication Reminder",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${reminder.date.dayOfMonth}/${reminder.date.monthNumber}/${reminder.date.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Instructions
                if (reminder.instructions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                
                // Recurring indicator
                if (reminder.isRecurring && reminder.recurringDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        val daysText = reminder.recurringDays.sorted().joinToString(", ") { day ->
                            when (day) {
                                0 -> "Sun"
                                1 -> "Mon"
                                2 -> "Tue"
                                3 -> "Wed"
                                4 -> "Thu"
                                5 -> "Fri"
                                6 -> "Sat"
                                else -> ""
                            }
                        }
                        Text(
                            text = "Recurring: $daysText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Taken status
                if (reminder.isTaken && reminder.takenAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Taken at ${String.format("%02d:%02d", reminder.takenAt.hour, reminder.takenAt.minute)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mark taken/untaken button
                IconButton(
                    onClick = onMarkTaken
                ) {
                    Icon(
                        imageVector = if (reminder.isTaken) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (reminder.isTaken) "Mark as not taken" else "Mark as taken",
                        tint = if (reminder.isTaken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reminder?") },
            text = { 
                Text(
                    if (medication != null) 
                        "Are you sure you want to delete the reminder for \"${medication.name}\"? This action cannot be undone."
                    else 
                        "Are you sure you want to delete this reminder? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
