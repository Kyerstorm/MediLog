package com.healthcalendar.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.ui.util.getWindowSizeClass
import com.healthcalendar.app.ui.util.WindowSizeClass
import com.healthcalendar.app.ui.util.responsivePadding
import com.healthcalendar.app.viewmodel.AppointmentViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var selectedDate by remember { mutableStateOf(now.date) }
    var currentMonth by remember { mutableStateOf(now.date) }

    // Use remember to avoid recalculating dates on every recomposition
    val (startOfMonth, endOfMonth) = remember(currentMonth) {
        val start = LocalDateTime(currentMonth.year, currentMonth.month, 1, 0, 0)
        val end = LocalDateTime(
            currentMonth.year,
            currentMonth.month,
            currentMonth.month.length(currentMonth.year % 4 == 0),
            23,
            59
        )
        start to end
    }

    // Use LaunchedEffect to properly manage Flow collection and cancellation
    var appointmentsInMonth by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    LaunchedEffect(currentMonth) {
        viewModel.getAppointmentsBetweenDates(startOfMonth, endOfMonth)
            .collect { appointments ->
                appointmentsInMonth = appointments
            }
    }
    
    // Filter out alarms - only show regular appointments in calendar
    val appointmentsOnly = remember(appointmentsInMonth) {
        appointmentsInMonth.filter { it.eventType != com.healthcalendar.app.data.database.entities.EventType.ALARM }
    }
    
    val appointmentsOnSelectedDate = appointmentsOnly.filter {
        it.startTime.date == selectedDate
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
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
                onClick = { 
                    navController.navigate(Screen.AddAppointment.createRoute(selectedDate.toString()))
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add appointment")
            }
        }
    ) { paddingValues ->
        val windowSize = getWindowSizeClass()
        val padding = responsivePadding()
        
        // Responsive calendar date circle sizes
        val dateCircleSize = when (windowSize) {
            WindowSizeClass.COMPACT -> 40.dp  // Phone: larger circles
            WindowSizeClass.MEDIUM -> 36.dp   // Small tablet: medium circles
            WindowSizeClass.EXPANDED -> 32.dp // Large tablet/foldable: smaller circles
        }
        
        // Responsive layout: Two-column for tablets, single column for phones
        if (windowSize == WindowSizeClass.EXPANDED) {
            // Tablet/Foldable: Two-column layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(padding)
            ) {
                // Left column: Calendar (narrower to prevent overlap)
                Column(
                    modifier = Modifier
                        .weight(0.45f) // Reduced from 1f to prevent calendar from being too wide
                        .fillMaxHeight()
                ) {
                    MonthSelector(
                        currentMonth = currentMonth,
                        onPreviousMonth = { currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH) },
                        onNextMonth = { currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    CalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        appointmentDates = appointmentsOnly.map { it.startTime.date }.toSet(),
                        dateCircleSize = dateCircleSize
                    )
                }
                
                // Vertical divider between columns
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .padding(vertical = 16.dp)
                )
                
                // Right column: Appointments list (wider for content)
                Column(
                    modifier = Modifier
                        .weight(0.55f) // More space for appointments
                        .fillMaxHeight()
                        .padding(start = padding)
                ) {
                    AppointmentsList(
                        selectedDate = selectedDate,
                        appointments = appointmentsOnSelectedDate
                    )
                }
            }
        } else {
            // Phone: Single column layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Month selector
                MonthSelector(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH) },
                    onNextMonth = { currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH) },
                    modifier = Modifier.padding(padding)
                )
                
                // Simple calendar grid
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    appointmentDates = appointmentsInMonth.map { it.startTime.date }.toSet(),
                    dateCircleSize = dateCircleSize
                )
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Appointments for selected date
                AppointmentsList(
                    selectedDate = selectedDate,
                    appointments = appointmentsOnSelectedDate,
                    modifier = Modifier.padding(horizontal = padding)
                )
            }
        }
    }
}

@Composable
private fun MonthSelector(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            
            Text(
                text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )
            
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next month")
            }
        }
    }
}

@Composable
private fun AppointmentsList(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    modifier: Modifier = Modifier
) {
    val viewModel: AppointmentViewModel = hiltViewModel()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Appointments on $selectedDate",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No appointments",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentItem(
                        appointment = appointment,
                        onDelete = {
                            appointmentToDelete = appointment
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Appointment") },
            text = { Text("Are you sure you want to delete '${appointmentToDelete?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        appointmentToDelete?.let { viewModel.deleteAppointment(it) }
                        showDeleteDialog = false
                        appointmentToDelete = null
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
}


@Composable
private fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    appointmentDates: Set<LocalDate>,
    dateCircleSize: Dp = 40.dp
) {
    val daysInMonth = currentMonth.month.length(currentMonth.year % 4 == 0)
    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Calendar days
            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOfWeek in 0..6) {
                        val shouldDisplay = week > 0 || dayOfWeek >= firstDayOfWeek
                        
                        if (shouldDisplay && dayCounter <= daysInMonth) {
                            val date = LocalDate(currentMonth.year, currentMonth.month, dayCounter)
                            val isSelected = date == selectedDate
                            val hasAppointment = appointmentDates.contains(date)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .width(dateCircleSize)
                                    .height(dateCircleSize)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayCounter.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (hasAppointment) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            }
                            dayCounter++
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentItem(
    appointment: com.healthcalendar.app.data.database.entities.Appointment,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatTime(appointment.startTime)} - ${formatTime(appointment.endTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (appointment.location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = appointment.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTime(dateTime: LocalDateTime): String {
    val hour = if (dateTime.hour == 0) 12 else if (dateTime.hour > 12) dateTime.hour - 12 else dateTime.hour
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute $amPm"
}
