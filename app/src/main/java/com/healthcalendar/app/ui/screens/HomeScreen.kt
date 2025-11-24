package com.healthcalendar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.ui.util.getWindowSizeClass
import com.healthcalendar.app.ui.util.responsivePadding
import com.healthcalendar.app.ui.util.WindowSizeClass
import com.healthcalendar.app.viewmodel.MedicationViewModel
import com.healthcalendar.app.viewmodel.AppointmentViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    medicationViewModel: MedicationViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel()
) {
    val medications by medicationViewModel.medications.collectAsState()
    val allAppointments by appointmentViewModel.getUpcomingAppointments()
        .collectAsState(initial = emptyList())
    
    // Filter out medication-related appointments
    val upcomingAppointments = remember(allAppointments) {
        allAppointments.filter { it.medicationId == null }
    }
    
    // Animation states
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Health Calendar") },
            actions = {
                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        val windowSize = getWindowSizeClass()
        val padding = responsivePadding()
        
        // Two-column layout for tablets
        if (windowSize == WindowSizeClass.EXPANDED) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left column: Welcome and Quick Actions
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { 
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                            modifier = Modifier.animateContentSize()
                        ) {
                            WelcomeCard() 
                        }
                    }
                    item { 
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 100)) + fadeIn(animationSpec = tween(delayMillis = 100)),
                            modifier = Modifier.animateContentSize()
                        ) {
                            QuickActionsCard(navController) 
                        }
                    }
                    
                    item {
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 200)) + fadeIn(animationSpec = tween(delayMillis = 200))
                        ) {
                            Text(
                                text = "Upcoming Appointments",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    if (upcomingAppointments.isEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 300)) + fadeIn(animationSpec = tween(delayMillis = 300))
                            ) {
                                EmptyStateCard(
                                    icon = Icons.Filled.DateRange,
                                    message = "No upcoming appointments",
                                    actionText = "View Calendar",
                                    onAction = { navController.navigate(Screen.Calendar.route) }
                                )
                            }
                        }
                    } else {
                        items(upcomingAppointments.take(3)) { appointment ->
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 300)) + fadeIn(animationSpec = tween(delayMillis = 300))
                            ) {
                                AppointmentCard(
                                    appointment = appointment,
                                    onEdit = { navController.navigate(Screen.EditAppointment.createRoute(appointment.id)) }
                                )
                            }
                        }
                    }
                }
                
                // Right column: Medications
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 200)) + fadeIn(animationSpec = tween(delayMillis = 200))
                        ) {
                            Text(
                                text = "Your Medications",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    if (medications.isEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 300)) + fadeIn(animationSpec = tween(delayMillis = 300))
                            ) {
                                EmptyStateCard(
                                    icon = Icons.Filled.Favorite,
                                    message = "No medications added yet",
                                    actionText = "Add Medication",
                                    onAction = { navController.navigate(Screen.AddMedication.route) }
                                )
                            }
                        }
                    } else {
                        items(medications.take(3)) { medication ->
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 300)) + fadeIn(animationSpec = tween(delayMillis = 300))
                            ) {
                                MedicationCard(
                                    medication = medication,
                                    onClick = { navController.navigate(Screen.MedicationDetail.createRoute(medication.id)) }
                                )
                            }
                        }
                        
                        if (medications.size > 3) {
                            item {
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 400)) + fadeIn(animationSpec = tween(delayMillis = 400))
                                ) {
                                    TextButton(
                                        onClick = { navController.navigate(Screen.MedicationList.route) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("View all medications (${medications.size})")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Single column for phones
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { 
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                        modifier = Modifier.animateContentSize()
                    ) {
                        WelcomeCard() 
                    }
                }
                item { 
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 100)) + fadeIn(animationSpec = tween(delayMillis = 100)),
                        modifier = Modifier.animateContentSize()
                    ) {
                        QuickActionsCard(navController) 
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 200)) + fadeIn(animationSpec = tween(delayMillis = 200))
                    ) {
                        Text(
                            text = "Your Medications",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                if (medications.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 300)) + fadeIn(animationSpec = tween(delayMillis = 300))
                        ) {
                            EmptyStateCard(
                                icon = Icons.Filled.Favorite,
                                message = "No medications added yet",
                                actionText = "Add Medication",
                                onAction = { navController.navigate(Screen.AddMedication.route) }
                            )
                        }
                    }
                } else {
                    items(medications.take(3)) { medication ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 300)) + fadeIn(animationSpec = tween(delayMillis = 300))
                        ) {
                            MedicationCard(
                                medication = medication,
                                onClick = { navController.navigate(Screen.MedicationDetail.createRoute(medication.id)) }
                            )
                        }
                    }
                    
                    if (medications.size > 3) {
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 400)) + fadeIn(animationSpec = tween(delayMillis = 400))
                            ) {
                                TextButton(
                                    onClick = { navController.navigate(Screen.MedicationList.route) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View all medications (${medications.size})")
                                }
                            }
                        }
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 500)) + fadeIn(animationSpec = tween(delayMillis = 500))
                    ) {
                        Text(
                            text = "Upcoming Appointments",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                if (upcomingAppointments.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 600)) + fadeIn(animationSpec = tween(delayMillis = 600))
                        ) {
                            EmptyStateCard(
                                icon = Icons.Filled.Assessment,
                                message = "Track your medication adherence",
                                actionText = "View Medical History",
                                onAction = { navController.navigate(Screen.MedicalHistory.route) }
                            )
                        }
                    }
                } else {
                    items(upcomingAppointments.take(3)) { appointment ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(delayMillis = 600)) + fadeIn(animationSpec = tween(delayMillis = 600))
                        ) {
                            AppointmentCard(
                                appointment = appointment,
                                onEdit = { navController.navigate(Screen.EditAppointment.createRoute(appointment.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val greeting = when (now.hour) {
                in 0..11 -> "Good morning"
                in 12..17 -> "Good afternoon"
                else -> "Good evening"
            }
            
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stay on top of your health today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun QuickActionsCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Filled.EditNote,
                    label = "View Notes",
                    onClick = { navController.navigate(Screen.NotesList.route) }
                )
                QuickActionButton(
                    icon = Icons.Filled.Assessment,
                    label = "Medical History",
                    onClick = { navController.navigate(Screen.MedicalHistory.route) }
                )
            }
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Filled.Search,
                    label = "NHS Search",
                    onClick = { navController.navigate(Screen.NHSSearch.route) }
                )
                QuickActionButton(
                    icon = Icons.Filled.Info,
                    label = "Scan Document",
                    onClick = { navController.navigate(Screen.DocumentScanner.route) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            interactionSource = interactionSource
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationCard(
    medication: com.healthcalendar.app.data.database.entities.Medication,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        interactionSource = interactionSource
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
                Text(
                    text = "${medication.amount} x ${medication.dosage} ${medication.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "View details"
            )
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: com.healthcalendar.app.data.database.entities.Appointment,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${appointment.startTime.date}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (appointment.location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = appointment.location,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit appointment"
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}
