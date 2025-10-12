package com.healthcalendar.app.ui.screens.calendar

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.viewmodel.AppointmentViewModel
import com.healthcalendar.app.viewmodel.MedicationViewModel
import kotlinx.datetime.*
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    navController: NavController,
    appointmentId: Long? = null,
    initialIsAlarm: Boolean = false,
    initialDate: String? = null,
    viewModel: AppointmentViewModel = hiltViewModel(),
    medicationViewModel: MedicationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isAlarm by remember { mutableStateOf(initialIsAlarm) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }
    var reminderDaysBefore by remember { mutableStateOf(0) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedMedicationId by remember { mutableStateOf<Long?>(null) } // Deprecated, kept for backward compatibility
    var selectedMedicationIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showMedicationPicker by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringPattern by remember { mutableStateOf<com.healthcalendar.app.data.database.entities.RecurringPattern?>(null) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    
    // Use initialDate if provided, otherwise use current date
    var selectedDate by remember { 
        mutableStateOf(
            if (initialDate != null) {
                try {
                    LocalDate.parse(initialDate)
                } catch (e: Exception) {
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
            } else {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
        )
    }
    var selectedTime by remember { 
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        mutableStateOf(LocalTime(now.hour, now.minute))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Contact picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        contactUri?.let {
            try {
                val cursor = context.contentResolver.query(
                    contactUri,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null, null, null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numberIndex >= 0) {
                            phoneNumber = it.getString(numberIndex) ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ContactPicker", "Error picking contact", e)
            }
        }
    }
    
    val medications by medicationViewModel.medications.collectAsState()
    val activeMedications = remember(medications) {
        medications.filter { it.isActive }
    }
    
    val isEditMode = appointmentId != null
    val screenTitle = when {
        isEditMode && isAlarm -> "Edit Alarm"
        isEditMode && !isAlarm -> "Edit Appointment"
        !isEditMode && isAlarm -> "New Alarm"
        else -> "New Appointment"
    }
    
    // Load appointment data when editing
    LaunchedEffect(appointmentId) {
        if (appointmentId != null && appointmentId > 0) {
            try {
                viewModel.getAppointmentById(appointmentId).collect { appointment ->
                    if (appointment != null) {
                        title = appointment.title
                        description = appointment.description
                        location = appointment.location
                        phoneNumber = appointment.phoneNumber
                        isAllDay = appointment.isAllDay
                        reminderDaysBefore = appointment.reminderDaysBefore
                        selectedDate = appointment.startTime.date
                        selectedTime = appointment.startTime.time
                        selectedMedicationId = appointment.medicationId
                        // Load multiple medication IDs (new field)
                        selectedMedicationIds = appointment.medicationIds.toSet()
                        // If using old single medication ID, migrate it to the set
                        if (appointment.medicationId != null && selectedMedicationIds.isEmpty()) {
                            selectedMedicationIds = setOf(appointment.medicationId)
                        }
                        isRecurring = appointment.isRecurring
                        recurringPattern = appointment.recurringPattern
                        // If the appointment is an alarm event, ensure the screen reflects that
                        try {
                            isAlarm = appointment.eventType == com.healthcalendar.app.data.database.entities.EventType.ALARM
                        } catch (e: Exception) {
                            // Ignore if eventType is missing or null
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error - appointment not found
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                }
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                leadingIcon = {
                    Icon(Icons.Filled.Info, contentDescription = null)
                }
            )
            
            // Medication selector (only for alarms)
            if (isAlarm) {
                OutlinedCard(
                    onClick = { showMedicationPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = null)
                            Column {
                                Text("Link to medications (optional)")
                                if (selectedMedicationIds.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val selectedMeds = activeMedications.filter { it.id in selectedMedicationIds }
                                    Text(
                                        text = selectedMeds.joinToString(", ") { it.name },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
            
            // Location (only for appointments)
            if (!isAlarm) {
                val appSettings by viewModel.appSettings.collectAsState()
                val recentLocations = appSettings?.recentLocations ?: emptyList()
                var showLocationDropdown by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.Place, contentDescription = null)
                            },
                            trailingIcon = {
                                if (recentLocations.isNotEmpty()) {
                                    IconButton(onClick = { showLocationDropdown = !showLocationDropdown }) {
                                        Icon(
                                            if (showLocationDropdown) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Recent locations"
                                        )
                                    }
                                }
                            }
                        )
                        
                        DropdownMenu(
                            expanded = showLocationDropdown,
                            onDismissRequest = { showLocationDropdown = false }
                        ) {
                            recentLocations.forEach { recentLocation ->
                                DropdownMenuItem(
                                    text = { Text(recentLocation) },
                                    onClick = {
                                        location = recentLocation
                                        showLocationDropdown = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.History, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                    
                    // Google Maps button
                    if (location.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=${Uri.encode(location)}")
                                )
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Map,
                                contentDescription = "Open in Maps",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Phone Number (only for appointments)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, contentDescription = null)
                        }
                    )
                    
                    // Contact picker button
                    IconButton(
                        onClick = { contactPickerLauncher.launch(null) }
                    ) {
                        Icon(
                            Icons.Filled.Contacts,
                            contentDescription = "Pick from contacts",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Call button
                    if (phoneNumber.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${phoneNumber.trim()}")
                                )
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Call,
                                contentDescription = "Call",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            
            // Reminder Options (only for appointments)
            if (!isAlarm) {
                OutlinedCard(
                    onClick = { showReminderDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Reminder", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                when (reminderDaysBefore) {
                                    0 -> "On the day"
                                    1 -> "1 day before"
                                    else -> "$reminderDaysBefore days before"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.Notifications, contentDescription = "Reminder")
                    }
                }
            }
            
            // ...existing code...
            
            // Date selector
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Text("Date: $selectedDate")
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            
            // Time selector (hide for all-day events)
            if (!isAllDay) {
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Time")
                            Text("Time: ${String.format("%02d:%02d", selectedTime.hour, selectedTime.minute)}")
                        }
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
            
            // Move toggles to be directly above the Save button
            Spacer(modifier = Modifier.height(12.dp))
            if (!isAlarm) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("All Day", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recurring", style = MaterialTheme.typography.bodyLarge)
                            if (isRecurring && recurringPattern != null) {
                                Text(
                                    when (recurringPattern) {
                                        com.healthcalendar.app.data.database.entities.RecurringPattern.DAILY -> "Every day"
                                        com.healthcalendar.app.data.database.entities.RecurringPattern.WEEKLY -> "Every week"
                                        com.healthcalendar.app.data.database.entities.RecurringPattern.BIWEEKLY -> "Every 2 weeks"
                                        com.healthcalendar.app.data.database.entities.RecurringPattern.MONTHLY -> "Every month"
                                        com.healthcalendar.app.data.database.entities.RecurringPattern.YEARLY -> "Every year"
                                        null -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isRecurring) {
                                IconButton(onClick = { showRecurringDialog = true }) {
                                    Icon(Icons.Filled.Repeat, contentDescription = "Change pattern", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = {
                                    isRecurring = it
                                    if (it && recurringPattern == null) {
                                        showRecurringDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val dateTime = LocalDateTime(selectedDate, selectedTime)
                    val endTime = if (isAllDay) {
                        // For all-day events, set end time to end of day
                        LocalDateTime(selectedDate, LocalTime(23, 59))
                    } else {
                        dateTime.toInstant(TimeZone.currentSystemDefault())
                            .plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault())
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                    }
                    
                    if (isEditMode && appointmentId != null) {
                        // Update existing appointment
                        viewModel.saveAppointment(
                            id = appointmentId,
                            title = title,
                            description = description,
                            location = location,
                            phoneNumber = phoneNumber,
                            startTime = dateTime,
                            endTime = endTime,
                            isAllDay = isAllDay,
                            reminderMinutesBefore = 30,
                            reminderDaysBefore = reminderDaysBefore,
                            category = com.healthcalendar.app.data.database.entities.AppointmentCategory.OTHER,
                            eventType = if (isAlarm) com.healthcalendar.app.data.database.entities.EventType.ALARM else com.healthcalendar.app.data.database.entities.EventType.APPOINTMENT,
                            medicationId = selectedMedicationIds.firstOrNull(), // Backward compatibility
                            medicationIds = selectedMedicationIds.toList(),
                            isRecurring = isRecurring,
                            recurringPattern = if (isRecurring) recurringPattern else null,
                            color = "#2196F3"
                        )
                    } else {
                        // Create new appointment
                        viewModel.addAppointment(
                            title = title,
                            description = description,
                            location = location,
                            phoneNumber = phoneNumber,
                            startTime = dateTime,
                            endTime = endTime,
                            isAllDay = isAllDay,
                            reminderDaysBefore = reminderDaysBefore,
                            eventType = if (isAlarm) com.healthcalendar.app.data.database.entities.EventType.ALARM else com.healthcalendar.app.data.database.entities.EventType.APPOINTMENT,
                            medicationId = selectedMedicationIds.firstOrNull(), // Backward compatibility
                            medicationIds = selectedMedicationIds.toList(),
                            isRecurring = isRecurring,
                            recurringPattern = if (isRecurring) recurringPattern else null
                        )
                    }
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditMode) "Update Appointment" else "Save Appointment")
            }
            
            
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
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
                TextButton(
                    onClick = {
                        selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
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
    
    // Medication Picker Dialog
    if (showMedicationPicker) {
        AlertDialog(
            onDismissRequest = { showMedicationPicker = false },
            title = { Text("Select Medications") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedMedicationIds.isNotEmpty()) {
                        Text(
                            text = "${selectedMedicationIds.size} medication(s) selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // Option to clear selection
                    OutlinedCard(
                        onClick = {
                            selectedMedicationIds = emptySet()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = null)
                            Text("Clear all selections")
                        }
                    }
                    
                    if (activeMedications.isEmpty()) {
                        Text(
                            text = "No active medications. Add medications first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        activeMedications.forEach { medication ->
                            val isSelected = medication.id in selectedMedicationIds
                            OutlinedCard(
                                onClick = {
                                    selectedMedicationIds = if (isSelected) {
                                        selectedMedicationIds - medication.id
                                    } else {
                                        selectedMedicationIds + medication.id
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null // Handled by card click
                                    )
                                    Icon(Icons.Filled.Favorite, contentDescription = null)
                                    Column {
                                        Text(
                                            text = medication.name,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${medication.amount} x ${medication.dosage} ${medication.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMedicationPicker = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMedicationPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Reminder Days Before Dialog
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Reminder") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Remind me:")
                    listOf(
                        0 to "On the day",
                        1 to "1 day before",
                        2 to "2 days before",
                        3 to "3 days before",
                        7 to "1 week before"
                    ).forEach { (days, label) ->
                        OutlinedCard(
                            onClick = {
                                reminderDaysBefore = days
                                showReminderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = reminderDaysBefore == days,
                                    onClick = {
                                        reminderDaysBefore = days
                                        showReminderDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Recurring Pattern Dialog
    if (showRecurringDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringDialog = false },
            title = { Text("Recurring Pattern") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Repeat:")
                    listOf(
                        com.healthcalendar.app.data.database.entities.RecurringPattern.DAILY to "Every day",
                        com.healthcalendar.app.data.database.entities.RecurringPattern.WEEKLY to "Every week",
                        com.healthcalendar.app.data.database.entities.RecurringPattern.BIWEEKLY to "Every 2 weeks",
                        com.healthcalendar.app.data.database.entities.RecurringPattern.MONTHLY to "Every month",
                        com.healthcalendar.app.data.database.entities.RecurringPattern.YEARLY to "Every year"
                    ).forEach { (pattern, label) ->
                        OutlinedCard(
                            onClick = {
                                recurringPattern = pattern
                                showRecurringDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = recurringPattern == pattern,
                                    onClick = {
                                        recurringPattern = pattern
                                        showRecurringDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRecurringDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

