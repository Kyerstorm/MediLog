package com.healthcalendar.app.ui.screens.medication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.api.LocalNHSMedicationService
import com.healthcalendar.app.data.database.entities.MedicationForm
import com.healthcalendar.app.viewmodel.MedicationViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationScreen(
    navController: NavController,
    medicationId: Long? = null,
    preFillName: String = "",
    preFillDosage: String = "",
    preFillUnit: String = "",
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val nhsService = remember { 
        LocalNHSMedicationService(context, com.google.gson.Gson())
    }
    
    var name by remember { mutableStateOf(preFillName) }
    var dosage by remember { mutableStateOf(preFillDosage) }
    var amount by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(preFillUnit) }
    var selectedForm by remember { mutableStateOf(MedicationForm.TABLET) }
    var instructions by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var showFormDropdown by remember { mutableStateOf(false) }
    
    // Reminder/Alarm state
    var enableReminder by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0,
        is24Hour = true
    )
    
    // Autocomplete state
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    
    // Debounced search for suggestions
    fun searchSuggestions(query: String) {
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = scope.launch {
                delay(300) // Debounce delay
                try {
                    suggestions = nhsService.getSuggestions(query)
                    showSuggestions = suggestions.isNotEmpty()
                } catch (e: Exception) {
                    suggestions = emptyList()
                    showSuggestions = false
                }
            }
        } else {
            suggestions = emptyList()
            showSuggestions = false
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = medicationId != null
    
    LaunchedEffect(medicationId) {
        if (medicationId != null) {
            viewModel.loadMedication(medicationId)
        }
    }
    
    val selectedMedication by viewModel.selectedMedication.collectAsState()

    LaunchedEffect(selectedMedication) {
        selectedMedication?.let { medication ->
            name = medication.name
            dosage = medication.dosage
            amount = medication.amount
            unit = medication.unit
            selectedForm = medication.form
            instructions = medication.instructions
            notes = medication.notes
            isActive = medication.isActive
        }
    }
    
    LaunchedEffect(uiState) {
        if (uiState is MedicationViewModel.UiState.Success) {
            navController.navigateUp()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Medication" else "Add Medication") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveMedication(
                                id = medicationId,
                                name = name,
                                dosage = dosage,
                                amount = amount,
                                unit = unit,
                                form = selectedForm,
                                instructions = instructions,
                                notes = notes,
                                isActive = isActive,
                                enableReminder = enableReminder,
                                reminderTime = if (enableReminder) reminderTime else null
                            )
                        },
                        enabled = name.isNotEmpty() && dosage.isNotEmpty() && amount.isNotEmpty() && unit.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Medication Name with Autocomplete
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newValue ->
                            name = newValue
                            searchSuggestions(newValue)
                        },
                        label = { Text("Medication Name *") },
                        placeholder = { Text("Start typing...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                        trailingIcon = {
                            if (showSuggestions && suggestions.isNotEmpty()) {
                                IconButton(onClick = { showSuggestions = false }) {
                                    Icon(Icons.Filled.ArrowDropUp, contentDescription = "Hide suggestions")
                                }
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showSuggestions && suggestions.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        suggestions.take(8).forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    name = suggestion
                                    showSuggestions = false
                                    suggestions = emptyList()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                        
                        if (suggestions.size > 8) {
                            Divider()
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "+${suggestions.size - 8} more results",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { },
                                enabled = false
                            )
                        }
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage per tablet *") },
                        placeholder = { Text("500") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount *") },
                        placeholder = { Text("1") },
                        modifier = Modifier.weight(0.7f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit *") },
                        placeholder = { Text("mg, ml") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }
            }
            
            item {
                ExposedDropdownMenuBox(
                    expanded = showFormDropdown,
                    onExpandedChange = { showFormDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedForm.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Form") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFormDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showFormDropdown,
                        onDismissRequest = { showFormDropdown = false }
                    ) {
                        MedicationForm.values().forEach { form ->
                            DropdownMenuItem(
                                text = { Text(form.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedForm = form
                                    showFormDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Currently Taking",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isActive) "This medication is active" else "This medication has been discontinued",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    placeholder = { Text("Take with food, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
            
            // Reminder Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Set Daily Reminder",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Get reminded to take your medication",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableReminder,
                        onCheckedChange = { enableReminder = it }
                    )
                }
            }
            
            if (enableReminder) {
                item {
                    OutlinedTextField(
                        value = reminderTime.ifEmpty { "Select time..." },
                        onValueChange = {},
                        label = { Text("Reminder Time") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true },
                        leadingIcon = {
                            Icon(Icons.Filled.Alarm, contentDescription = "Time")
                        },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    placeholder = { Text("Additional information...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
            }
            
            item {
                if (uiState is MedicationViewModel.UiState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                
                if (uiState is MedicationViewModel.UiState.Error) {
                    Text(
                        text = (uiState as MedicationViewModel.UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
