package com.healthcalendar.app.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.healthcalendar.app.ui.screens.HomeScreen
import com.healthcalendar.app.ui.screens.AlarmsScreen
import com.healthcalendar.app.ui.screens.MedicalHistoryScreen
import com.healthcalendar.app.ui.screens.SettingsScreenEnhanced
import com.healthcalendar.app.ui.screens.calendar.CalendarScreen
import com.healthcalendar.app.ui.screens.calendar.AddEditAppointmentScreen
import com.healthcalendar.app.ui.screens.calendar.DailyScheduleScreen
import com.healthcalendar.app.ui.screens.medication.MedicationListScreen
import com.healthcalendar.app.ui.screens.medication.AddEditMedicationScreen
import com.healthcalendar.app.ui.screens.medication.MedicationDetailScreen
import com.healthcalendar.app.ui.screens.medication.MedicationHistoryScreen
import com.healthcalendar.app.ui.screens.medication.NHSMedicationSearchScreen
import com.healthcalendar.app.ui.screens.documents.DocumentScannerScreen
import com.healthcalendar.app.ui.screens.documents.DocumentViewerScreen
import com.healthcalendar.app.ui.screens.notes.NotesListScreen
import com.healthcalendar.app.ui.screens.notes.AddEditNoteScreen
import kotlin.math.abs

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Alarms : Screen("alarms")
    object AddAppointment : Screen("add_appointment?date={date}") {
        fun createRoute(date: String? = null) = if (date != null) "add_appointment?date=$date" else "add_appointment"
    }
    object AddAlarm : Screen("add_alarm")
    object EditAppointment : Screen("edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: Long) = "edit_appointment/$appointmentId"
    }
    object DailySchedule : Screen("daily_schedule/{year}/{month}/{day}") {
        fun createRoute(year: Int, month: Int, day: Int) = "daily_schedule/$year/$month/$day"
    }
    object MedicationList : Screen("medications")
    object AddMedication : Screen("add_medication?name={name}&dosage={dosage}&unit={unit}") {
        fun createRoute(name: String = "", dosage: String = "", unit: String = "") = 
            "add_medication?name=$name&dosage=$dosage&unit=$unit"
    }
    object EditMedication : Screen("edit_medication/{medicationId}") {
        fun createRoute(medicationId: Long) = "edit_medication/$medicationId"
    }
    object MedicationDetail : Screen("medication_detail/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication_detail/$medicationId"
    }
    object MedicationHistory : Screen("medication_history/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication_history/$medicationId"
    }
    object MedicalHistory : Screen("medical_history")
    object NHSSearch : Screen("nhs_search")
    object DocumentScanner : Screen("document_scanner")
    object DocumentViewer : Screen("document_viewer/{documentId}") {
        fun createRoute(documentId: Long) = "document_viewer/$documentId"
    }
    object Settings : Screen("settings")
    object NotesList : Screen("notes")
    object AddNote : Screen("add_note")
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "edit_note/$noteId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Main navigation routes in order
    val mainRoutes = listOf(
        Screen.Home.route,
        Screen.Calendar.route,
        Screen.MedicationList.route,
        Screen.Alarms.route
    )
    
    // Swipe threshold
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    
    Scaffold(
        bottomBar = {
            // Only show bottom navigation on main screens
            when (currentRoute) {
                Screen.Home.route,
                Screen.Calendar.route,
                Screen.MedicationList.route,
                Screen.Alarms.route -> {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                if (currentRoute != Screen.Home.route) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.DateRange, contentDescription = "Calendar") },
                            label = { Text("Calendar") },
                            selected = currentRoute == Screen.Calendar.route,
                            onClick = {
                                if (currentRoute != Screen.Calendar.route) {
                                    navController.navigate(Screen.Calendar.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Medication, contentDescription = "Medications") },
                            label = { Text("Medications") },
                            selected = currentRoute == Screen.MedicationList.route,
                            onClick = {
                                if (currentRoute != Screen.MedicationList.route) {
                                    navController.navigate(Screen.MedicationList.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Alarm, contentDescription = "Alarms") },
                            label = { Text("Alarms") },
                            selected = currentRoute == Screen.Alarms.route,
                            onClick = {
                                if (currentRoute != Screen.Alarms.route) {
                                    navController.navigate(Screen.Alarms.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        var dragOffset by remember { mutableStateOf(0f) }
        
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .pointerInput(currentRoute) {
                    // Only enable swipe gestures on main screens
                    if (currentRoute in mainRoutes) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragOffset = 0f },
                            onDragEnd = {
                                // Check if swipe threshold was met
                                if (abs(dragOffset) > swipeThreshold) {
                                    val currentIndex = mainRoutes.indexOf(currentRoute)
                                    val nextIndex = if (dragOffset < 0) {
                                        // Swipe left - next screen
                                        (currentIndex + 1).coerceAtMost(mainRoutes.size - 1)
                                    } else {
                                        // Swipe right - previous screen
                                        (currentIndex - 1).coerceAtLeast(0)
                                    }
                                    
                                    if (nextIndex != currentIndex) {
                                        val nextRoute = mainRoutes[nextIndex]
                                        navController.navigate(nextRoute) {
                                            popUpTo(Screen.Home.route)
                                            launchSingleTop = true
                                        }
                                    }
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = { dragOffset = 0f }
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    }
                }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState.destination.route in listOf(Screen.Calendar.route, Screen.MedicationList.route, Screen.Alarms.route)) -it else it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { if (targetState.destination.route in listOf(Screen.Calendar.route, Screen.MedicationList.route, Screen.Alarms.route)) -it else it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            }
        ) {
            HomeScreen(navController = navController)
        }
        
        composable(
            route = Screen.Calendar.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { if (initialState.destination.route == Screen.Home.route) it else -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { if (targetState.destination.route == Screen.Home.route) it else -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            }
        ) {
            CalendarScreen(navController = navController)
        }
        
        composable(
            route = Screen.Alarms.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { if (initialState.destination.route in listOf(Screen.Home.route, Screen.Calendar.route, Screen.MedicationList.route)) it else -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { if (targetState.destination.route in listOf(Screen.Home.route, Screen.Calendar.route, Screen.MedicationList.route)) it else -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            }
        ) {
            AlarmsScreen(navController = navController)
        }
        
        composable(
            route = Screen.AddAppointment.route,
            arguments = listOf(navArgument("date") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date")
            AddEditAppointmentScreen(
                navController = navController,
                appointmentId = null,
                initialDate = dateString
            )
        }
        
        composable(Screen.AddAlarm.route) {
            AddEditAppointmentScreen(
                navController = navController,
                appointmentId = null,
                initialIsAlarm = true
            )
        }
        
        composable(
            route = Screen.EditAppointment.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getLong("appointmentId")
            AddEditAppointmentScreen(
                navController = navController,
                appointmentId = appointmentId,
                initialDate = null
            )
        }
        
        composable(
            route = Screen.DailySchedule.route,
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            val month = backStackEntry.arguments?.getInt("month") ?: 1
            val day = backStackEntry.arguments?.getInt("day") ?: 1
            DailyScheduleScreen(
                navController = navController,
                selectedDate = kotlinx.datetime.LocalDate(year, month, day)
            )
        }
        
        composable(
            route = Screen.MedicationList.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { if (initialState.destination.route in listOf(Screen.Home.route, Screen.Calendar.route)) it else -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { if (targetState.destination.route in listOf(Screen.Home.route, Screen.Calendar.route)) it else -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            }
        ) {
            MedicationListScreen(navController = navController)
        }
        
        composable(
            route = Screen.AddMedication.route,
            arguments = listOf(
                navArgument("name") { 
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("dosage") { 
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("unit") { 
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val dosage = backStackEntry.arguments?.getString("dosage") ?: ""
            val unit = backStackEntry.arguments?.getString("unit") ?: ""
            AddEditMedicationScreen(
                navController = navController,
                medicationId = null,
                preFillName = name,
                preFillDosage = dosage,
                preFillUnit = unit
            )
        }
        
        composable(
            route = Screen.EditMedication.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId")
            AddEditMedicationScreen(
                navController = navController,
                medicationId = medicationId
            )
        }
        
        composable(
            route = Screen.MedicationDetail.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId")
            MedicationDetailScreen(
                navController = navController,
                medicationId = medicationId ?: 0L
            )
        }
        
        composable(
            route = Screen.MedicationHistory.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId")
            MedicationHistoryScreen(
                navController = navController,
                medicationId = medicationId ?: 0L
            )
        }
        
        composable(Screen.MedicalHistory.route) {
            MedicalHistoryScreen(navController = navController)
        }
        
        composable(Screen.NHSSearch.route) {
            NHSMedicationSearchScreen(navController = navController)
        }
        
        composable(Screen.DocumentScanner.route) {
            DocumentScannerScreen(navController = navController)
        }
        
        composable(
            route = Screen.DocumentViewer.route,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId")
            DocumentViewerScreen(
                navController = navController,
                documentId = documentId ?: 0L
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreenEnhanced(navController = navController)
        }
        
        // Notes navigation
        composable(Screen.NotesList.route) {
            NotesListScreen(navController = navController)
        }
        
        composable(Screen.AddNote.route) {
            AddEditNoteScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId")
            AddEditNoteScreen(navController = navController, noteId = noteId)
        }
            }
        }
    }
}

