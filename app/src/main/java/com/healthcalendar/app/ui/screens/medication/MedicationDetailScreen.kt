package com.healthcalendar.app.ui.screens.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.database.entities.MedicationSchedule
import com.healthcalendar.app.data.database.entities.ScheduleFrequency
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.viewmodel.MedicationViewModel
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
    navController: NavController,
    medicationId: Long,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medication by viewModel.selectedMedication.collectAsState()
    val schedules by viewModel.medicationSchedules.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(medicationId) {
        viewModel.loadMedication(medicationId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is MedicationViewModel.UiState.Success && medication == null) {
            navController.navigateUp()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(medication?.name ?: "Medication Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(com.healthcalendar.app.ui.navigation.Screen.MedicationHistory.createRoute(medicationId)) }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                    IconButton(onClick = { navController.navigate(Screen.EditMedication.createRoute(medicationId)) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
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
                onClick = { showAddScheduleDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add schedule")
            }
        }
    ) { paddingValues ->
        medication?.let { med ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            
                            DetailRow(
                                label = "Dosage",
                                value = "${med.dosage} ${med.unit}"
                            )
                            
                            DetailRow(
                                label = "Form",
                                value = med.form.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                            
                            if (med.instructions.isNotEmpty()) {
                                DetailRow(
                                    label = "Instructions",
                                    value = med.instructions
                                )
                            }
                            
                            if (med.notes.isNotEmpty()) {
                                DetailRow(
                                    label = "Notes",
                                    value = med.notes
                                )
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "Schedules",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (schedules.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No schedules set",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = { showAddScheduleDialog = true }) {
                                    Text("Add Schedule")
                                }
                            }
                        }
                    }
                } else {
                    items(schedules) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { enabled ->
                                viewModel.toggleSchedule(schedule.id, enabled)
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication?") },
            text = { Text("Are you sure you want to delete this medication? All schedules will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        medication?.let { viewModel.deleteMedication(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showAddScheduleDialog) {
        AddScheduleDialog(
            medicationId = medicationId,
            onDismiss = { showAddScheduleDialog = false },
            onConfirm = { schedule ->
                viewModel.addSchedule(schedule)
                showAddScheduleDialog = false
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: MedicationSchedule,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTime(schedule.time),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFrequency(schedule.frequency, schedule.daysOfWeek),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = schedule.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(
    medicationId: Long,
    onDismiss: () -> Unit,
    onConfirm: (MedicationSchedule) -> Unit
) {
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedFrequency by remember { mutableStateOf(ScheduleFrequency.DAILY) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time picker (simplified)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time:", style = MaterialTheme.typography.bodyLarge)
                    
                    // Hour picker
                    OutlinedTextField(
                        value = selectedHour.toString().padStart(2, '0'),
                        onValueChange = { if (it.toIntOrNull() in 0..23) selectedHour = it.toInt() },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    
                    // Minute picker
                    OutlinedTextField(
                        value = selectedMinute.toString().padStart(2, '0'),
                        onValueChange = { if (it.toIntOrNull() in 0..59) selectedMinute = it.toInt() },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                }
                
                Text("Frequency:", style = MaterialTheme.typography.bodyLarge)
                
                Column {
                    ScheduleFrequency.values().forEach { frequency ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFrequency == frequency,
                                onClick = { selectedFrequency = frequency }
                            )
                            Text(frequency.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
                
                if (selectedFrequency == ScheduleFrequency.WEEKLY) {
                    Text("Days:", style = MaterialTheme.typography.bodyLarge)
                    
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    FlowRow {
                        days.forEachIndexed { index, day ->
                            val dayNumber = index + 1
                            FilterChip(
                                selected = selectedDays.contains(dayNumber),
                                onClick = {
                                    selectedDays = if (selectedDays.contains(dayNumber)) {
                                        selectedDays - dayNumber
                                    } else {
                                        selectedDays + dayNumber
                                    }
                                },
                                label = { Text(day) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val schedule = MedicationSchedule(
                        medicationId = medicationId,
                        time = LocalTime(selectedHour, selectedMinute),
                        frequency = selectedFrequency,
                        daysOfWeek = selectedDays.toList().sorted(),
                        isEnabled = true
                    )
                    onConfirm(schedule)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Simple flow row implementation
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        content()
    }
}

private fun formatTime(time: LocalTime): String {
    val hour = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
    val amPm = if (time.hour < 12) "AM" else "PM"
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute $amPm"
}

private fun formatFrequency(frequency: ScheduleFrequency, daysOfWeek: List<Int>): String {
    return when (frequency) {
        ScheduleFrequency.DAILY -> "Every day"
        ScheduleFrequency.WEEKLY -> {
            if (daysOfWeek.isEmpty()) "Weekly"
            else {
                val dayNames = daysOfWeek.map {
                    when (it) {
                        1 -> "Mon"
                        2 -> "Tue"
                        3 -> "Wed"
                        4 -> "Thu"
                        5 -> "Fri"
                        6 -> "Sat"
                        7 -> "Sun"
                        else -> ""
                    }
                }
                dayNames.joinToString(", ")
            }
        }
        ScheduleFrequency.AS_NEEDED -> "As needed"
        ScheduleFrequency.CUSTOM -> "Custom"
    }
}
