package com.healthcalendar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthcalendar.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settingsState by settingsViewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Passcode Length Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Passcode Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Passcode Length",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 6, 8).forEach { length ->
                            FilterChip(
                                selected = settingsState.passcodeLength == length,
                                onClick = { settingsViewModel.setPasscodeLength(length) },
                                label = { Text("$length digits") }
                            )
                        }
                    }

                    Divider()

                    // Alphanumeric option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Alphanumeric Password",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Use letters and numbers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settingsState.allowAlphanumeric,
                            onCheckedChange = { settingsViewModel.setAllowAlphanumeric(it) }
                        )
                    }
                }
            }

            // Auto-Lock Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Auto-Lock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Auto-Lock")
                        Switch(
                            checked = settingsState.autoLockEnabled,
                            onCheckedChange = { settingsViewModel.setAutoLockEnabled(it) }
                        )
                    }

                    if (settingsState.autoLockEnabled) {
                        Text(
                            "Auto-Lock Timeout",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                0 to "Immediate",
                                1 to "1 min",
                                5 to "5 min",
                                15 to "15 min",
                                30 to "30 min"
                            ).forEach { (minutes, label) ->
                                FilterChip(
                                    selected = settingsState.autoLockTimeoutMinutes == minutes,
                                    onClick = { settingsViewModel.setAutoLockTimeout(minutes) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lock on Screen Off")
                            Switch(
                                checked = settingsState.lockOnScreenOff,
                                onCheckedChange = { settingsViewModel.setLockOnScreenOff(it) }
                            )
                        }
                    }
                }
            }

            // Failed Attempts Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Failed Attempts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Maximum Failed Attempts",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 5, 10).forEach { max ->
                            FilterChip(
                                selected = settingsState.maxFailedAttempts == max,
                                onClick = { settingsViewModel.setMaxFailedAttempts(max) },
                                label = { Text("$max attempts") }
                            )
                        }
                    }

                    Text(
                        "After exceeding limit, temporary lockout increases progressively: 30s → 1min → 5min → 15min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Emergency Access Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Emergency Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { /* TODO: Open security question dialog */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (settingsState.securityQuestion != null)
                                "Change Security Question"
                            else
                                "Set Security Question"
                        )
                    }

                    OutlinedButton(
                        onClick = { /* TODO: Open recovery email dialog */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (settingsState.recoveryEmail != null)
                                "Change Recovery Email"
                            else
                                "Set Recovery Email"
                        )
                    }

                    if (settingsState.securityQuestion != null || settingsState.recoveryEmail != null) {
                        Text(
                            "Emergency access methods let you recover if you forget your passcode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
