package com.healthcalendar.app.ui.screens.calendar

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
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationReminder
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.viewmodel.AppointmentViewModel
import com.healthcalendar.app.viewmodel.MedicationReminderViewModel
import com.healthcalendar.app.viewmodel.MedicationViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScheduleScreen(
    navController: NavController,
    selectedDate: LocalDate,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    reminderViewModel: MedicationReminderViewModel = hiltViewModel(),
    medicationViewModel: MedicationViewModel = hiltViewModel()
) {
    val appointments by appointmentViewModel.getAppointmentsForDate(selectedDate)
        .collectAsState(initial = emptyList())
    val reminders by reminderViewModel.getRemindersForDate(selectedDate)
        .collectAsState(initial = emptyList())
    val medications by medicationViewModel.medications.collectAsState()
    
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    
    // Group reminders by time for multiple medications at same time
    val remindersByTime = reminders.groupBy { it.time }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule for ${selectedDate.dayOfMonth} ${selectedDate.month.name}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                onClick = { showAddReminderDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add reminder")
            }
        }
    ) { paddingValues ->
        if (appointments.isEmpty() && reminders.isEmpty()) {
            // Empty state
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
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No schedule for this day",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Add medication reminders or appointments",
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sort all items by time
                val allItems = buildList {
                    // Add appointments
                    appointments.forEach { appointment ->
                        add(appointment.startTime.time to appointment)
                    }
                    
                    // Add medication reminders (grouped by time)
                    remindersByTime.entries.forEach { (time, reminderList) ->
                        add(time to reminderList)
                    }
                }
                
                // Sort by time
                val sortedItems = allItems.sortedBy { it.first }
                
                items(sortedItems) { (time, item) ->
                    when (item) {
                        is Appointment -> {
                            AppointmentTimeSlot(
                                appointment = item,
                                onClick = {
                                    navController.navigate(Screen.EditAppointment.createRoute(item.id))
                                },
                                onDelete = {
                                    appointmentViewModel.deleteAppointment(item)
                                }
                            )
                        }
                        is List<*> -> {
                            val reminderList = item.filterIsInstance<MedicationReminder>()
                            MedicationReminderTimeSlot(
                                time = time,
                                reminders = reminderList,
                                medications = medications,
                                onReminderClick = { reminder ->
                                    selectedTime = reminder.time
                                },
                                onMarkTaken = { reminder ->
                                    reminderViewModel.markReminderTaken(
                                        reminder.id,
                                        !reminder.isTaken,
                                        if (!reminder.isTaken) Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) else null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add medication reminder dialog
    if (showAddReminderDialog) {
        AddMedicationReminderDialog(
            selectedDate = selectedDate,
            medications = medications,
            onDismiss = { showAddReminderDialog = false },
            onSave = { medicationIds, time, dosage, instructions ->
                // Create reminders for multiple medications at the same time
                medicationIds.forEach { medicationId ->
                    reminderViewModel.addReminder(
                        medicationId = medicationId,
                        date = selectedDate,
                        time = time,
                        dosage = dosage,
                        instructions = instructions
                    )
                }
                showAddReminderDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentTimeSlot(
    appointment: Appointment,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
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
                    .background(Color(android.graphics.Color.parseColor(appointment.color)).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", appointment.startTime.hour, appointment.startTime.minute),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(android.graphics.Color.parseColor(appointment.color))
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (appointment.eventType == com.healthcalendar.app.data.database.entities.EventType.ALARM) {
                            Icons.Filled.Notifications
                        } else {
                            Icons.Filled.DateRange
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (appointment.eventType == com.healthcalendar.app.data.database.entities.EventType.ALARM) {
                            "ALARM"
                        } else {
                            "APPOINTMENT"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (appointment.location.isNotEmpty()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                if (onDelete != null) {
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
            title = { Text("Delete ${if (appointment.eventType == com.healthcalendar.app.data.database.entities.EventType.ALARM) "Alarm" else "Appointment"}?") },
            text = { Text("Are you sure you want to delete \"${appointment.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
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
private fun MedicationReminderTimeSlot(
    time: LocalTime,
    reminders: List<MedicationReminder>,
    medications: List<Medication>,
    onReminderClick: (MedicationReminder) -> Unit,
    onMarkTaken: (MedicationReminder) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Time header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d:%02d", time.hour, time.minute),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "MEDICATION${if (reminders.size > 1) "S" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${reminders.size} medication${if (reminders.size > 1) "s" else ""} to take",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // List of medications for this time
            reminders.forEach { reminder ->
                val medication = medications.find { it.id == reminder.medicationId }
                if (medication != null) {
                    MedicationReminderItem(
                        reminder = reminder,
                        medication = medication,
                        onMarkTaken = { onMarkTaken(reminder) }
                    )
                    if (reminder != reminders.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationReminderItem(
    reminder: MedicationReminder,
    medication: Medication,
    onMarkTaken: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = reminder.isTaken,
            onCheckedChange = { onMarkTaken() }
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleSmall,
                textDecoration = if (reminder.isTaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
            Text(
                text = "${reminder.dosage} - ${medication.form.name.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (reminder.instructions.isNotEmpty()) {
                Text(
                    text = reminder.instructions,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (reminder.isTaken && reminder.takenAt != null) {
                Text(
                    text = "Taken at ${String.format("%02d:%02d", reminder.takenAt.hour, reminder.takenAt.minute)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (reminder.alarmEnabled) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Alarm enabled",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicationReminderDialog(
    selectedDate: LocalDate,
    medications: List<Medication>,
    onDismiss: () -> Unit,
    onSave: (List<Long>, LocalTime, String, String) -> Unit
) {
    var selectedMedicationIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedTime by remember { mutableStateOf(LocalTime(8, 0)) }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medication Reminder") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Select medications (multiple allowed)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                items(medications) { medication ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedMedicationIds.contains(medication.id),
                            onCheckedChange = { checked ->
                                selectedMedicationIds = if (checked) {
                                    selectedMedicationIds + medication.id
                                } else {
                                    selectedMedicationIds - medication.id
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = medication.name)
                            Text(
                                text = "${medication.amount} x ${medication.dosage} ${medication.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Time: ${String.format("%02d:%02d", selectedTime.hour, selectedTime.minute)}")
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g., 2 tablets)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedMedicationIds.isNotEmpty() && dosage.isNotEmpty()) {
                        onSave(selectedMedicationIds.toList(), selectedTime, dosage, instructions)
                    }
                },
                enabled = selectedMedicationIds.isNotEmpty() && dosage.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
