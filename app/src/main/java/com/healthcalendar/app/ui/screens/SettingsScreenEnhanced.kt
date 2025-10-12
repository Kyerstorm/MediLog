package com.healthcalendar.app.ui.screens

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.preferences.UserPreferencesRepository
import com.healthcalendar.app.viewmodel.SettingsViewModel
import com.healthcalendar.app.viewmodel.ThemeViewModel
import com.healthcalendar.app.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenEnhanced(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Collect all state
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val amoledMode by viewModel.amoledMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val notificationSound by viewModel.notificationSound.collectAsState()
    val vibrate by viewModel.vibrate.collectAsState()
    val autoBackup by viewModel.autoBackup.collectAsState()
    val backupFrequency by viewModel.backupFrequency.collectAsState()
    val exportFormat by viewModel.exportFormat.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val backupList by viewModel.backupList.collectAsState()
    
    // Dialog states
    var showThemeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showBackupFrequencyDialog by remember { mutableStateOf(false) }
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var showBackupListDialog by remember { mutableStateOf(false) }
    var showFontScaleDialog by remember { mutableStateOf(false) }
    var showCustomThemeDialog by remember { mutableStateOf(false) }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importData(it) }
    }
    
    // File saver for export - use CreateDocument instead of sharing
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.saveExportToUri(it)
        }
    }
    
    // Handle export status - trigger file saver
    LaunchedEffect(exportStatus) {
        when (val status = exportStatus) {
            is SettingsViewModel.OperationStatus.Success -> {
                status.file?.let {
                    // Trigger file picker with suggested filename
                    val extension = status.file.extension
                    val baseName = status.file.nameWithoutExtension
                    exportLauncher.launch("$baseName.$extension")
                }
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // === APPEARANCE SECTION ===
            item {
                SectionHeader("Appearance")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "Theme",
                    subtitle = when (themeMode) {
                        UserPreferencesRepository.ThemeMode.LIGHT -> "Light"
                        UserPreferencesRepository.ThemeMode.DARK -> "Dark"
                        UserPreferencesRepository.ThemeMode.SYSTEM -> "System default"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Brush,
                    title = "Custom Theme Maker",
                    subtitle = "Create and apply your own color set",
                    onClick = { showCustomThemeDialog = true }
                )
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.ColorLens,
                    title = "Dynamic Colors",
                    subtitle = "Material You colors (Android 12+)",
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
            
            
            // === ACCESSIBILITY SECTION ===
            item {
                SectionHeader("Accessibility")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.FormatSize,
                    title = "Font Size",
                    subtitle = "${(fontScale * 100).toInt()}%",
                    onClick = { showFontScaleDialog = true }
                )
            }
            
            // High Contrast and AMOLED toggles (shortcuts) in Accessibility — also apply themes when toggled
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Contrast,
                    title = "High Contrast",
                    subtitle = "Increase text and UI contrast",
                    checked = highContrast,
                    onCheckedChange = { enabled ->
                        viewModel.setHighContrast(enabled)
                        if (enabled) {
                            themeViewModel.setTheme(AppTheme.HIGH_CONTRAST)
                        } else {
                            themeViewModel.setTheme(AppTheme.DEFAULT)
                        }
                    }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.DarkMode,
                    title = "AMOLED Mode",
                    subtitle = "Pure black theme for OLED screens",
                    checked = amoledMode,
                    onCheckedChange = { enabled ->
                        viewModel.setAmoledMode(enabled)
                        if (enabled) {
                            themeViewModel.setTheme(AppTheme.AMOLED)
                            themeViewModel.setDarkMode(true)
                        } else {
                            themeViewModel.setTheme(AppTheme.DEFAULT)
                            themeViewModel.setDarkMode(false)
                        }
                    }
                )
            }
            
            // === LANGUAGE SECTION ===
            item {
                SectionHeader("Language & Region")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Language",
                    subtitle = language.displayName,
                    onClick = { showLanguageDialog = true }
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Note: UI text translations are not yet implemented. This setting affects system locale only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // === NOTIFICATIONS SECTION ===
            item {
                SectionHeader("Notifications")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notification Sound",
                    subtitle = notificationSound.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showNotificationDialog = true }
                )
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Vibration,
                    title = "Vibrate",
                    subtitle = "Vibrate on notifications",
                    checked = vibrate,
                    onCheckedChange = { viewModel.setVibrate(it) }
                )
            }
            
            // === BACKUP & RESTORE SECTION ===
            item {
                SectionHeader("Backup & Restore")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Backup,
                    title = "Create Backup",
                    subtitle = "Encrypted backup of all data",
                    onClick = { viewModel.createBackup() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Restore,
                    title = "Restore from Backup",
                    subtitle = "${backupList.size} backups available",
                    onClick = { showBackupListDialog = true }
                )
            }
            
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.AutoMode,
                    title = "Auto Backup",
                    subtitle = "Automatic scheduled backups",
                    checked = autoBackup,
                    onCheckedChange = { viewModel.setAutoBackup(it) }
                )
            }
            
            if (autoBackup) {
                item {
                    SettingsItem(
                        icon = Icons.Filled.Schedule,
                        title = "Backup Frequency",
                        subtitle = backupFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { showBackupFrequencyDialog = true }
                    )
                }
            }
            
            // === DATA MANAGEMENT SECTION ===
            item {
                SectionHeader("Data Management")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.FileUpload,
                    title = "Import Data",
                    subtitle = "Import from CSV or JSON file",
                    onClick = { importLauncher.launch("*/*") }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.FileDownload,
                    title = "Export Data",
                    subtitle = "Export to ${exportFormat.name}",
                    onClick = { showExportDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Export Format",
                    subtitle = exportFormat.name,
                    onClick = { showExportFormatDialog = true }
                )
            }
            
            // === ABOUT SECTION ===
            item {
                SectionHeader("About")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Build 15)",
                    onClick = { }
                )
            }
        }
    }
    
    // === DIALOGS ===
    
    // Theme Dialog
    if (showThemeDialog) {
        SelectionDialog(
            title = "Choose Theme",
            options = UserPreferencesRepository.ThemeMode.values().map { 
                it.name.lowercase().replaceFirstChar { c -> c.uppercase() } to it 
            },
            selected = themeMode,
            onSelect = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Custom Theme Dialog (show when the Settings item is clicked)
    if (showCustomThemeDialog) {
        // Use a dialog that contains a simple color wheel + hex input for primary/secondary/background
        CustomThemeDialog(
            onDismiss = { showCustomThemeDialog = false },
            onSave = { primaryHex, secondaryHex, backgroundHex, useCustom ->
                // Same persistence as before
                fun hexToRgb(hex: String): Triple<Int, Int, Int> {
                    val c = hex.removePrefix("#")
                    val r = Integer.parseInt(c.substring(0,2), 16)
                    val g = Integer.parseInt(c.substring(2,4), 16)
                    val b = Integer.parseInt(c.substring(4,6), 16)
                    return Triple(r,g,b)
                }

                val (pR, pG, pB) = hexToRgb(primaryHex)
                val (sR, sG, sB) = hexToRgb(secondaryHex)
                val (bR, bG, bB) = hexToRgb(backgroundHex)

                fun onColorFor(r: Int, g: Int, b: Int) = if ((r*0.299 + g*0.587 + b*0.114) > 186) "#000000" else "#FFFFFF"

                val obj = org.json.JSONObject().apply {
                    put("primary", primaryHex)
                    put("onPrimary", onColorFor(pR,pG,pB))
                    put("primaryContainer", primaryHex)
                    put("onPrimaryContainer", onColorFor(pR,pG,pB))

                    put("secondary", secondaryHex)
                    put("onSecondary", onColorFor(sR,sG,sB))
                    put("secondaryContainer", secondaryHex)
                    put("onSecondaryContainer", onColorFor(sR,sG,sB))

                    put("background", backgroundHex)
                    put("onBackground", onColorFor(bR,bG,bB))
                    put("surface", backgroundHex)
                    put("onSurface", onColorFor(bR,bG,bB))

                    put("error", "#B00020")
                    put("onError", "#FFFFFF")
                    put("outline", "#79747E")
                }

                themeViewModel.setCustomThemeJson(obj.toString())
                themeViewModel.setUseCustomTheme(useCustom)
                showCustomThemeDialog = false
            }
        )
    }

    // ...existing code...
    
    // Language Dialog  
    if (showLanguageDialog) {
        SelectionDialog(
            title = "Choose Language",
            options = UserPreferencesRepository.Language.values().map { it.displayName to it },
            selected = language,
            onSelect = {
                viewModel.setLanguage(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Notification Sound Dialog with preview
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notification Sound") },
            text = {
                Column {
                    UserPreferencesRepository.NotificationSound.values().forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setNotificationSound(sound)
                                    // Play sound preview
                                    playNotificationSound(context, sound)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sound == notificationSound,
                                onClick = {
                                    viewModel.setNotificationSound(sound)
                                    playNotificationSound(context, sound)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sound.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
    
    // Backup Frequency Dialog
    if (showBackupFrequencyDialog) {
        SelectionDialog(
            title = "Backup Frequency",
            options = UserPreferencesRepository.BackupFrequency.values().map {
                it.name.lowercase().replaceFirstChar { c -> c.uppercase() } to it
            },
            selected = backupFrequency,
            onSelect = {
                viewModel.setBackupFrequency(it)
                showBackupFrequencyDialog = false
            },
            onDismiss = { showBackupFrequencyDialog = false }
        )
    }
    
    // Export Format Dialog
    if (showExportFormatDialog) {
        SelectionDialog(
            title = "Export Format",
            options = UserPreferencesRepository.ExportFormat.values().map { it.name to it },
            selected = exportFormat,
            onSelect = {
                viewModel.setExportFormat(it)
                showExportFormatDialog = false
            },
            onDismiss = { showExportFormatDialog = false }
        )
    }
    
    // Font Scale Dialog
    if (showFontScaleDialog) {
        FontScaleDialog(
            currentScale = fontScale,
            onScaleChange = { viewModel.setFontScale(it) },
            onDismiss = { showFontScaleDialog = false }
        )
    }
    
    // Export Options Dialog
    if (showExportDialog) {
        ExportDialog(
            isLoading = exportStatus is SettingsViewModel.OperationStatus.Loading,
            onExportAll = { viewModel.exportAllData() },
            onExportAppointments = { viewModel.exportAppointments() },
            onExportMedications = { viewModel.exportMedications() },
            onDismiss = { showExportDialog = false }
        )
    }
    
    // Backup List Dialog
    if (showBackupListDialog) {
        BackupListDialog(
            backups = backupList,
            onRestore = { 
                viewModel.restoreBackup(it)
                showBackupListDialog = false
            },
            onDelete = { viewModel.deleteBackup(it) },
            onDismiss = { showBackupListDialog = false }
        )
    }
    
    // Status Snackbars with Toast notifications
    when (val status = importStatus) {
        is SettingsViewModel.OperationStatus.Success -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                viewModel.resetImportStatus()
            }
        }
        is SettingsViewModel.OperationStatus.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Import failed: ${status.message}", Toast.LENGTH_LONG).show()
                viewModel.resetImportStatus()
            }
        }
        else -> {}
    }
    
    when (val status = backupStatus) {
        is SettingsViewModel.OperationStatus.Success -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, status.message.ifEmpty { "Backup created successfully" }, Toast.LENGTH_LONG).show()
                viewModel.loadBackupList()
                viewModel.resetBackupStatus()
            }
        }
        is SettingsViewModel.OperationStatus.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Backup failed: ${status.message}", Toast.LENGTH_LONG).show()
                viewModel.resetBackupStatus()
            }
        }
        is SettingsViewModel.OperationStatus.Loading -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Creating backup...", Toast.LENGTH_SHORT).show()
            }
        }
        else -> {}
    }
    
    when (val status = exportStatus) {
        is SettingsViewModel.OperationStatus.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Export failed: ${status.message}", Toast.LENGTH_LONG).show()
                viewModel.resetExportStatus()
            }
        }
        else -> {}
    }
}


@Composable
private fun CustomThemeDialog(
    onDismiss: () -> Unit,
    onSave: (primaryHex: String, secondaryHex: String, backgroundHex: String, useCustom: Boolean) -> Unit
) {
    // Helper to convert RGB ints to hex string
    fun rgbToHex(r: Int, g: Int, b: Int): String = String.format("#%02X%02X%02X", r.coerceIn(0,255), g.coerceIn(0,255), b.coerceIn(0,255))

    // Primary color sliders
    var pR by remember { mutableStateOf(33) }
    var pG by remember { mutableStateOf(150) }
    var pB by remember { mutableStateOf(243) }

    // Secondary color sliders
    var sR by remember { mutableStateOf(3) }
    var sG by remember { mutableStateOf(169) }
    var sB by remember { mutableStateOf(244) }

    // Background color sliders
    var bR by remember { mutableStateOf(255) }
    var bG by remember { mutableStateOf(255) }
    var bB by remember { mutableStateOf(255) }

    var useCustom by remember { mutableStateOf(true) }

    val primaryHex = rgbToHex(pR, pG, pB)
    val secondaryHex = rgbToHex(sR, sG, sB)
    val backgroundHex = rgbToHex(bR, bG, bB)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Theme Maker") },
        text = {
            Column {
                // Replace RGB sliders with simple HSV wheel pickers and hex input
                Column {
                    var primaryHexState by remember { mutableStateOf(primaryHex) }
                    var secondaryHexState by remember { mutableStateOf(secondaryHex) }
                    var backgroundHexState by remember { mutableStateOf(backgroundHex) }

                    Text("Primary")
                    SimpleColorWheel(selectedColorHex = primaryHexState, onColorSelected = { primaryHexState = it })
                    OutlinedTextField(value = primaryHexState, onValueChange = { primaryHexState = it }, label = { Text("Primary Hex") })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Secondary")
                    SimpleColorWheel(selectedColorHex = secondaryHexState, onColorSelected = { secondaryHexState = it })
                    OutlinedTextField(value = secondaryHexState, onValueChange = { secondaryHexState = it }, label = { Text("Secondary Hex") })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Background")
                    SimpleColorWheel(selectedColorHex = backgroundHexState, onColorSelected = { backgroundHexState = it })
                    OutlinedTextField(value = backgroundHexState, onValueChange = { backgroundHexState = it }, label = { Text("Background Hex") })

                    // update the original hex variables for saving
                    LaunchedEffect(primaryHexState, secondaryHexState, backgroundHexState) {
                        // parse hex to RGB ints for compatibility with save logic
                        fun hexToRgbInts(hex: String): Triple<Int, Int, Int> {
                            return try {
                                val c = hex.removePrefix("#")
                                Triple(Integer.parseInt(c.substring(0,2), 16), Integer.parseInt(c.substring(2,4), 16), Integer.parseInt(c.substring(4,6), 16))
                            } catch (e: Exception) { Triple(0,0,0) }
                        }
                        val (pr, pg, pb) = hexToRgbInts(primaryHexState)
                        pR = pr; pG = pg; pB = pb
                        val (sr, sg, sb) = hexToRgbInts(secondaryHexState)
                        sR = sr; sG = sg; sB = sb
                        val (br, bg, bb) = hexToRgbInts(backgroundHexState)
                        bR = br; bG = bg; bB = bb
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Expanded JSON will include derived onPrimary and surface values (simple inversion/contrast heuristics)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(primaryHex)))) {
                        Text("Primary", modifier = Modifier.padding(8.dp), color = Color.White)
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(secondaryHex)))) {
                        Text("Secondary", modifier = Modifier.padding(8.dp), color = Color.White)
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(backgroundHex)))) {
                        Text("Background", modifier = Modifier.padding(8.dp), color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useCustom, onCheckedChange = { useCustom = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply custom theme")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Build expanded JSON with additional derived fields
                val obj = org.json.JSONObject().apply {
                    put("primary", primaryHex)
                    put("onPrimary", if ((pR*0.299 + pG*0.587 + pB*0.114) > 186) "#000000" else "#FFFFFF")
                    put("primaryContainer", primaryHex)
                    put("onPrimaryContainer", if ((pR*0.299 + pG*0.587 + pB*0.114) > 186) "#000000" else "#FFFFFF")

                    put("secondary", secondaryHex)
                    put("onSecondary", if ((sR*0.299 + sG*0.587 + sB*0.114) > 186) "#000000" else "#FFFFFF")
                    put("secondaryContainer", secondaryHex)
                    put("onSecondaryContainer", if ((sR*0.299 + sG*0.587 + sB*0.114) > 186) "#000000" else "#FFFFFF")

                    put("background", backgroundHex)
                    put("onBackground", if ((bR*0.299 + bG*0.587 + bB*0.114) > 186) "#000000" else "#FFFFFF")
                    put("surface", backgroundHex)
                    put("onSurface", if ((bR*0.299 + bG*0.587 + bB*0.114) > 186) "#000000" else "#FFFFFF")

                    // Keep some defaults for other slots
                    put("error", "#B00020")
                    put("onError", "#FFFFFF")
                    put("outline", "#79747E")
                }

                onSave(primaryHex, secondaryHex, backgroundHex, useCustom)
                // Also save the full JSON via ThemeViewModel in caller
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
private fun SliderWithLabels(value: Float, onValueChange: (Float) -> Unit, label: String) {
    Column {
        Text(label)
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..255f)
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun <T> SelectionDialog(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == value,
                            onClick = { onSelect(value) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FontScaleDialog(
    currentScale: Float,
    onScaleChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableStateOf(currentScale) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Font Size") },
        text = {
            Column {
                Text("${(sliderValue * 100).toInt()}%")
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0.8f..1.5f,
                    steps = 6
                )
                Text(
                    text = "Preview: Sample Text",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * sliderValue
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onScaleChange(sliderValue)
                onDismiss()
            }) {
                Text("Apply")
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
private fun ExportDialog(
    isLoading: Boolean,
    onExportAll: () -> Unit,
    onExportAppointments: () -> Unit,
    onExportMedications: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Data") },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportOption(
                        title = "All Data",
                        subtitle = "Appointments, medications, and logs",
                        onClick = onExportAll
                    )
                    Divider()
                    ExportOption(
                        title = "Appointments Only",
                        subtitle = "Export all appointments",
                        onClick = onExportAppointments
                    )
                    Divider()
                    ExportOption(
                        title = "Medications Only",
                        subtitle = "Export all medications",
                        onClick = onExportMedications
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExportOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackupListDialog(
    backups: List<java.io.File>,
    onRestore: (java.io.File) -> Unit,
    onDelete: (java.io.File) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Available Backups") },
        text = {
            if (backups.isEmpty()) {
                Text("No backups available")
            } else {
                LazyColumn {
                    items(backups) { backup ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateFormat.format(Date(backup.lastModified())),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${backup.length() / 1024} KB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onRestore(backup) }) {
                                    Icon(Icons.Filled.Restore, "Restore")
                                }
                                IconButton(onClick = { onDelete(backup) }) {
                                    Icon(Icons.Filled.Delete, "Delete")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Helper function to play notification sounds (5 second preview)
private fun playNotificationSound(context: Context, sound: UserPreferencesRepository.NotificationSound) {
    try {
        val uri = when (sound) {
            UserPreferencesRepository.NotificationSound.DEFAULT -> 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            UserPreferencesRepository.NotificationSound.GENTLE -> 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            UserPreferencesRepository.NotificationSound.ALERT -> 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            UserPreferencesRepository.NotificationSound.SILENT -> null
        }
        
        uri?.let {
            val ringtone = RingtoneManager.getRingtone(context, it)
            ringtone?.play()
            
            // Stop after 5 seconds
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    ringtone?.stop()
                } catch (e: Exception) {
                    // Ignore
                }
            }, 5000)
        }
    } catch (e: Exception) {
        // Ignore errors
    }
}

@Composable
private fun SimpleColorWheel(selectedColorHex: String, onColorSelected: (String) -> Unit) {
    // Basic HSV sliders representing a simple color wheel replacement
    var hue by remember { mutableStateOf(200f) }
    var sat by remember { mutableStateOf(0.7f) }
    var value by remember { mutableStateOf(0.95f) }

    fun hsvToHex(h: Float, s: Float, v: Float): String {
        val color = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
        return String.format("#%06X", 0xFFFFFF and color)
    }

    Column {
        Text("Hue: ${hue.toInt()}")
        Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)
        Text("Saturation: ${(sat*100).toInt()}%")
        Slider(value = sat, onValueChange = { sat = it }, valueRange = 0f..1f)
        Text("Value: ${(value*100).toInt()}%")
        Slider(value = value, onValueChange = { value = it }, valueRange = 0f..1f)
        Spacer(modifier = Modifier.height(8.dp))
        val hex = hsvToHex(hue, sat, value)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(hex)))) {
                Text(" ", modifier = Modifier.size(48.dp))
            }
            Text(hex)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { onColorSelected(hex) }) { Text("Use") }
        }
    }
}
