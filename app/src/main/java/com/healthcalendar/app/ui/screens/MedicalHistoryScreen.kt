package com.healthcalendar.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.database.entities.MedicationLog
import com.healthcalendar.app.data.database.entities.MedicationStatus
import com.healthcalendar.app.viewmodel.MedicalHistoryViewModel
import com.healthcalendar.app.ui.util.getWindowSizeClass
import com.healthcalendar.app.ui.util.WindowSizeClass
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    navController: NavController,
    viewModel: MedicalHistoryViewModel = hiltViewModel()
) {
    val statistics by viewModel.statistics.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val windowSize = getWindowSizeClass()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadStatistics() }) {
                        Icon(Icons.Filled.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is MedicalHistoryViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MedicalHistoryViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Error loading statistics: ${(uiState as MedicalHistoryViewModel.UiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is MedicalHistoryViewModel.UiState.Success -> {
                MedicalHistoryContent(
                    statistics = statistics,
                    windowSize = windowSize,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun MedicalHistoryContent(
    statistics: com.healthcalendar.app.viewmodel.MedicalHistoryStatistics,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Cards
        item {
            Text(
                "Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            QuickStatsRow(statistics, windowSize)
        }
        
        // Adherence Chart
        item {
            Text(
                "Weekly Adherence",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            AdherenceChart(
                data = statistics.adherenceData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        
        // Per-Medication Breakdown
        item {
            Text(
                "Medication Breakdown",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(statistics.perMedicationStats) { stat ->
            MedicationStatCard(stat)
        }
        
        // Recent Activity
        if (statistics.recentLogs.isNotEmpty()) {
            item {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(statistics.recentLogs.take(10)) { log ->
                RecentActivityItem(log)
            }
        }
    }
}

@Composable
fun QuickStatsRow(
    statistics: com.healthcalendar.app.viewmodel.MedicalHistoryStatistics,
    windowSize: WindowSizeClass
) {
    val columns = when (windowSize) {
        WindowSizeClass.COMPACT -> 2
        WindowSizeClass.MEDIUM -> 3
        WindowSizeClass.EXPANDED -> 4
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Active Medications",
                value = statistics.totalActiveMedications.toString(),
                icon = Icons.Filled.MedicalServices,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Current Streak",
                value = "${statistics.currentStreak} days",
                icon = Icons.Filled.LocalFireDepartment,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Today",
                value = "${statistics.todayAdherence.roundToInt()}%",
                icon = Icons.Filled.Today,
                color = getAdherenceColor(statistics.todayAdherence),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "This Week",
                value = "${statistics.weeklyAdherence.roundToInt()}%",
                icon = Icons.Filled.CalendarMonth,
                color = getAdherenceColor(statistics.weeklyAdherence),
                modifier = Modifier.weight(1f)
            )
        }
        
        if (windowSize != WindowSizeClass.COMPACT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "This Month",
                    value = "${statistics.monthlyAdherence.roundToInt()}%",
                    icon = Icons.Filled.CalendarToday,
                    color = getAdherenceColor(statistics.monthlyAdherence),
                    modifier = Modifier.weight(1f)
                )
                // Empty spacer for alignment
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun AdherenceChart(
    data: List<com.healthcalendar.app.viewmodel.AdherenceDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Card(modifier = modifier) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No data available")
            }
        }
        return
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Card(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val width = size.width
            val height = size.height
            val maxValue = 100f
            val spacing = width / (data.size - 1).coerceAtLeast(1)
            
            // Draw grid lines
            val gridColor = Color.Gray.copy(alpha = 0.2f)
            for (i in 0..4) {
                val y = height * i / 4
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            
            // Draw line chart
            val path = Path()
            data.forEachIndexed { index, point ->
                val x = index * spacing
                val y = height - (point.adherence / maxValue * height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw point
                drawCircle(
                    color = getAdherenceColor(point.adherence),
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
            
            // Draw path
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun MedicationStatCard(stat: com.healthcalendar.app.viewmodel.PerMedicationStat) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stat.totalTaken} / ${stat.totalScheduled} doses taken",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${stat.adherenceRate.roundToInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = getAdherenceColor(stat.adherenceRate)
            )
        }
    }
}

@Composable
fun RecentActivityItem(log: com.healthcalendar.app.data.database.entities.MedicationLog) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${log.scheduledTime.date} ${log.scheduledTime.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            val (icon, color) = when (log.status) {
                MedicationStatus.TAKEN -> Icons.Filled.CheckCircle to Color.Green
                MedicationStatus.SKIPPED -> Icons.Filled.Cancel to Color.Red
                MedicationStatus.PENDING -> Icons.Filled.PendingActions to Color.Gray
                MedicationStatus.MISSED -> Icons.Filled.Cancel to Color(0xFFFF9800) // Orange
            }
            
            Icon(
                imageVector = icon,
                contentDescription = log.status.name,
                tint = color
            )
        }
    }
}

fun getAdherenceColor(adherence: Float): Color {
    return when {
        adherence >= 80f -> Color(0xFF4CAF50) // Green
        adherence >= 50f -> Color(0xFFFFA726) // Orange
        else -> Color(0xFFEF5350) // Red
    }
}
