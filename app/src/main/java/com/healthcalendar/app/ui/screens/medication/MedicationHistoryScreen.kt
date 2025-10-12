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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.database.entities.MedicationStatus
import com.healthcalendar.app.viewmodel.MedicationViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationHistoryScreen(
    navController: NavController,
    medicationId: Long,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medication by viewModel.getMedicationFlow(medicationId).collectAsState(initial = null)
    val logs by viewModel.getMedicationLogs(medicationId).collectAsState(initial = emptyList())
    
    // Calculate statistics
    val stats = remember(logs) {
        if (logs.isEmpty()) {
            MedicationStats(0, 0, 0, 0f)
        } else {
            val totalDoses = logs.size
            val takenDoses = logs.count { it.status == MedicationStatus.TAKEN }
            val missedDoses = logs.count { it.status == MedicationStatus.MISSED }
            val adherenceRate = if (totalDoses > 0) (takenDoses.toFloat() / totalDoses * 100) else 0f
            MedicationStats(totalDoses, takenDoses, missedDoses, adherenceRate)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medication History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
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
            // Medication info header
            item {
                medication?.let { med ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${med.dosage} • ${med.form}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Statistics cards
            item {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Adherence Rate
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Adherence",
                        value = "${stats.adherenceRate.roundToInt()}%",
                        icon = Icons.Filled.TrendingUp,
                        color = when {
                            stats.adherenceRate >= 90 -> MaterialTheme.colorScheme.primary
                            stats.adherenceRate >= 70 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    // Total Doses
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Doses",
                        value = stats.totalDoses.toString(),
                        icon = Icons.Filled.LocalHospital,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Taken
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Taken",
                        value = stats.takenDoses.toString(),
                        icon = Icons.Filled.CheckCircle,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Missed
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Missed",
                        value = stats.missedDoses.toString(),
                        icon = Icons.Filled.Cancel,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // History header
            item {
                Text(
                    text = "Recent History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Log entries
            items(logs.sortedByDescending { it.scheduledTime }) { log ->
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
                                text = "${log.scheduledTime.date}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format(
                                    "%02d:%02d",
                                    log.scheduledTime.hour,
                                    log.scheduledTime.minute
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            log.takenTime?.let { taken ->
                                Text(
                                    text = "Taken at ${String.format("%02d:%02d", taken.hour, taken.minute)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Status badge
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when (log.status) {
                                MedicationStatus.TAKEN -> MaterialTheme.colorScheme.primaryContainer
                                MedicationStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
                                MedicationStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
                                MedicationStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (log.status) {
                                        MedicationStatus.TAKEN -> Icons.Filled.CheckCircle
                                        MedicationStatus.MISSED -> Icons.Filled.Cancel
                                        MedicationStatus.SKIPPED -> Icons.Filled.Forward
                                        MedicationStatus.PENDING -> Icons.Filled.Schedule
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = when (log.status) {
                                        MedicationStatus.TAKEN -> MaterialTheme.colorScheme.primary
                                        MedicationStatus.MISSED -> MaterialTheme.colorScheme.error
                                        MedicationStatus.SKIPPED -> MaterialTheme.colorScheme.tertiary
                                        MedicationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = when (log.status) {
                                        MedicationStatus.TAKEN -> "Taken"
                                        MedicationStatus.MISSED -> "Missed"
                                        MedicationStatus.SKIPPED -> "Skipped"
                                        MedicationStatus.PENDING -> "Pending"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when (log.status) {
                                        MedicationStatus.TAKEN -> MaterialTheme.colorScheme.onPrimaryContainer
                                        MedicationStatus.MISSED -> MaterialTheme.colorScheme.onErrorContainer
                                        MedicationStatus.SKIPPED -> MaterialTheme.colorScheme.onTertiaryContainer
                                        MedicationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Empty state
            if (logs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No history yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Your medication history will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class MedicationStats(
    val totalDoses: Int,
    val takenDoses: Int,
    val missedDoses: Int,
    val adherenceRate: Float
)
