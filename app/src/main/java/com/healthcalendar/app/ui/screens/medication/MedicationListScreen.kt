package com.healthcalendar.app.ui.screens.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.ui.util.getWindowSizeClass
import com.healthcalendar.app.ui.util.gridColumns
import com.healthcalendar.app.ui.util.WindowSizeClass
import com.healthcalendar.app.viewmodel.MedicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    navController: NavController,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medications by viewModel.allMedications.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val showDiscontinued by viewModel.showDiscontinued.collectAsState()
    
    // Count discontinued medications
    val discontinuedCount = medications.count { !it.isActive }

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Medications") },
                actions = {
                    // Toggle discontinued medications (icon is always visible; no badge)
                    IconButton(onClick = {
                        viewModel.toggleShowDiscontinued()
                        android.util.Log.d("MedicationList", "Toggle discontinued (VM): $showDiscontinued")
                    }) {
                        Icon(
                            if (showDiscontinued) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showDiscontinued) "Hide discontinued" else "Show discontinued",
                            tint = if (showDiscontinued) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                onClick = { navController.navigate(Screen.AddMedication.route) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add medication")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search medications...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            if (medications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No medications added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { navController.navigate(Screen.AddMedication.route) }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Medication")
                        }
                    }
                }
            } else {
                val windowSize = getWindowSizeClass()
                val columns = gridColumns()
                
                val filteredMedications = if (searchQuery.isEmpty()) {
                    medications
                } else {
                    medications.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                
                // Separate active and discontinued medications, sorted A-Z
                val activeMedications = filteredMedications.filter { it.isActive }.sortedBy { it.name.lowercase() }
                val discontinuedMedications = filteredMedications.filter { !it.isActive }.sortedBy { it.name.lowercase() }
                
                // Debug logging
                android.util.Log.d("MedicationList", "Active: ${activeMedications.size}, Discontinued: ${discontinuedMedications.size}, ShowDiscontinued: $showDiscontinued")
                
                // Use grid for tablets, list for phones
                if (windowSize == WindowSizeClass.COMPACT) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Active Medications Section
                        if (activeMedications.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Active Medications (${activeMedications.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(items = activeMedications, key = { it.id }) { medication ->
                                MedicationListItem(
                                    medication = medication,
                                    onClick = {
                                        navController.navigate(Screen.MedicationDetail.createRoute(medication.id))
                                    }
                                )
                            }
                        }
                        
                        // Discontinued Medications Section (only show if toggle is on)
                        if (showDiscontinued && discontinuedMedications.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 2.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Discontinued Medications",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = "${discontinuedMedications.size}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            items(items = discontinuedMedications, key = { it.id }) { medication ->
                                MedicationListItem(
                                    medication = medication,
                                    onClick = {
                                        navController.navigate(Screen.MedicationDetail.createRoute(medication.id))
                                    },
                                    isDiscontinued = true
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Active Medications Section
                        if (activeMedications.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Active Medications (${activeMedications.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columns),
                                    modifier = Modifier.height(((activeMedications.size + columns - 1) / columns * 120).dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(items = activeMedications, key = { it.id }) { medication ->
                                        MedicationListItem(
                                            medication = medication,
                                            onClick = {
                                                navController.navigate(Screen.MedicationDetail.createRoute(medication.id))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Discontinued Medications Section (only show if toggle is on)
                        if (showDiscontinued && discontinuedMedications.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 2.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Discontinued Medications",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = "${discontinuedMedications.size}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columns),
                                    modifier = Modifier.height(((discontinuedMedications.size + columns - 1) / columns * 120).dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(items = discontinuedMedications, key = { it.id }) { medication ->
                                        MedicationListItem(
                                            medication = medication,
                                            onClick = {
                                                navController.navigate(Screen.MedicationDetail.createRoute(medication.id))
                                            },
                                            isDiscontinued = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationListItem(
    medication: com.healthcalendar.app.data.database.entities.Medication,
    onClick: () -> Unit,
    isDiscontinued: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDiscontinued) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscontinued) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
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
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${medication.amount} x ${medication.dosage} ${medication.unit} - ${medication.form.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (medication.instructions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = medication.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
