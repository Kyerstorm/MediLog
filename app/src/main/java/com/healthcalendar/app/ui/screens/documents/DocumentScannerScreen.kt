package com.healthcalendar.app.ui.screens.documents

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.healthcalendar.app.data.database.entities.DocumentType
import com.healthcalendar.app.ui.navigation.Screen
import com.healthcalendar.app.ui.util.getWindowSizeClass
import com.healthcalendar.app.ui.util.gridColumns
import com.healthcalendar.app.ui.util.responsivePadding
import com.healthcalendar.app.util.scanner.DocumentScanner
import com.healthcalendar.app.viewmodel.DocumentScannerViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.minus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    navController: NavController,
    viewModel: DocumentScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val allDocuments by viewModel.allDocuments.collectAsState()
    val scanningState by viewModel.scanningState.collectAsState()
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var scannedUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    
    // File picker launcher for PDF/Word documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadedFileUri = it
            showUploadDialog = true
        }
    }
    
    // Scanner launcher - properly handles the ML Kit scanner result
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val scanResult = com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(intent)
                scanResult?.let {
                    // Get the first page URI
                    val uris = mutableListOf<Uri>()
                    
                    // Get PDF if available
                    it.pdf?.let { pdf ->
                        uris.add(pdf.uri)
                    }
                    
                    // Or get the first page image
                    if (uris.isEmpty() && it.pages != null && it.pages!!.isNotEmpty()) {
                        uris.add(it.pages!![0].imageUri)
                    }
                    
                    if (uris.isNotEmpty()) {
                        scannedUri = uris.first()
                        showSaveDialog = true
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanned Documents") },
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
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action menu
                if (showActionMenu) {
                    // Upload file button
                    SmallFloatingActionButton(
                        onClick = {
                            showActionMenu = false
                            // Accept PDF and Word documents
                            filePickerLauncher.launch("application/*")
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Upload file")
                            Text("Upload File")
                        }
                    }
                    
                    // Scan document button
                    SmallFloatingActionButton(
                        onClick = {
                            showActionMenu = false
                            activity?.let { act ->
                                // Initialize scanner
                                val options = com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.Builder()
                                    .setGalleryImportAllowed(true)
                                    .setPageLimit(10)
                                    .setScannerMode(com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                                    .build()
                                
                                val scanner = com.google.mlkit.vision.documentscanner.GmsDocumentScanning.getClient(options)
                                
                                // Start scanner
                                scanner.getStartScanIntent(act)
                                    .addOnSuccessListener { intentSender ->
                                        scanLauncher.launch(
                                            androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        viewModel.setScanningError(e.message ?: "Failed to start scanner")
                                    }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Scan document")
                            Text("Scan Document")
                        }
                    }
                }
                
                // Main FAB
                FloatingActionButton(
                    onClick = { showActionMenu = !showActionMenu }
                ) {
                    Icon(
                        imageVector = if (showActionMenu) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (showActionMenu) "Close menu" else "Add document"
                    )
                }
            }
        }
    ) { paddingValues ->
        when (scanningState) {
            is DocumentScannerViewModel.ScanningState.Saving -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DocumentScannerViewModel.ScanningState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = (scanningState as DocumentScannerViewModel.ScanningState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.resetScanningState() }) {
                            Text("Try Again")
                        }
                    }
                }
            }
            else -> {
                val columns = gridColumns()
                val padding = responsivePadding()
                
                if (allDocuments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No scanned documents yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the + button to scan a document",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Use grid for tablets, list for phones
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(padding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allDocuments) { document ->
                            DocumentCard(
                                document = document,
                                onClick = { 
                                    navController.navigate(Screen.DocumentViewer.createRoute(document.id))
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showSaveDialog && scannedUri != null) {
        SaveDocumentDialog(
            onDismiss = {
                showSaveDialog = false
                scannedUri = null
            },
            onSave = { title, description, type ->
                viewModel.saveScannedDocument(
                    title = title,
                    description = description,
                    fileUri = scannedUri!!,
                    documentType = type
                )
                showSaveDialog = false
                scannedUri = null
            }
        )
    }
    
    if (showUploadDialog && uploadedFileUri != null) {
        UploadDocumentDialog(
            uri = uploadedFileUri!!,
            context = context,
            onDismiss = {
                showUploadDialog = false
                uploadedFileUri = null
            },
            onSave = { title, description, type, mimeType, fileSize ->
                viewModel.saveUploadedDocument(
                    title = title,
                    description = description,
                    fileUri = uploadedFileUri!!,
                    documentType = type,
                    mimeType = mimeType,
                    fileSize = fileSize
                )
                showUploadDialog = false
                uploadedFileUri = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentCard(
    document: com.healthcalendar.app.data.database.entities.ScannedDocumentEntity,
    onClick: () -> Unit,
    viewModel: DocumentScannerViewModel
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Document type icon with color - UNIFIED ICON FOR ALL TYPES
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Document title
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Pin button
                    IconButton(
                        onClick = { viewModel.togglePin(document.id, document.isPinned) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (document.isPinned) Icons.Filled.PushPin else Icons.Filled.Add,
                            contentDescription = if (document.isPinned) "Unpin" else "Pin",
                            tint = if (document.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Document type with icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = document.documentType.name
                            .replace("_", " ")
                            .lowercase()
                            .split(" ")
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Scan date
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatScanDate(document.scannedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "View document",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to format scan date
private fun formatScanDate(dateTime: kotlinx.datetime.LocalDateTime): String {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    
    val date = dateTime.date
    val today = now.date
    
    // Calculate yesterday using DatePeriod
    val yesterday = today.minus(DatePeriod(days = 1))
    
    return when {
        date == today -> "Today at ${String.format("%02d:%02d", dateTime.hour, dateTime.minute)}"
        date == yesterday -> 
            "Yesterday at ${String.format("%02d:%02d", dateTime.hour, dateTime.minute)}"
        date.year == today.year -> 
            "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} at ${String.format("%02d:%02d", dateTime.hour, dateTime.minute)}"
        else -> 
            "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} ${date.year}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveDocumentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, DocumentType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DocumentType.OTHER) }
    var showTypeMenu by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Scanned Document") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                ExposedDropdownMenuBox(
                    expanded = showTypeMenu,
                    onExpandedChange = { showTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Document Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        DocumentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedType = type
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, description, selectedType) },
                enabled = title.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadDocumentDialog(
    uri: Uri,
    context: android.content.Context,
    onDismiss: () -> Unit,
    onSave: (String, String, DocumentType, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DocumentType.PDF_DOCUMENT) }
    var showTypeMenu by remember { mutableStateOf(false) }
    
    // Get file information
    val (mimeType, fileSize, fileName) = remember(uri) {
        val contentResolver = context.contentResolver
        val mime = contentResolver.getType(uri) ?: "application/octet-stream"
        var size = 0L
        var name = "document"
        
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        
        Triple(mime, size, name)
    }
    
    // Auto-detect document type from mime type and set default title
    LaunchedEffect(mimeType, fileName) {
        title = fileName.substringBeforeLast(".")
        
        selectedType = when {
            mimeType.contains("pdf") -> DocumentType.PDF_DOCUMENT
            mimeType.contains("word") || mimeType.contains("msword") || mimeType.contains("wordprocessingml") -> DocumentType.WORD_DOCUMENT
            mimeType.contains("image") -> DocumentType.SCANNED_IMAGE
            else -> DocumentType.OTHER
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Document") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // File info card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                mimeType.contains("pdf") -> Icons.Filled.Info
                                mimeType.contains("word") -> Icons.Filled.Create
                                else -> Icons.Filled.Info
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.2f", fileSize / 1024.0)} KB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                ExposedDropdownMenuBox(
                    expanded = showTypeMenu,
                    onExpandedChange = { showTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Document Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        DocumentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedType = type
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, description, selectedType, mimeType, fileSize) },
                enabled = title.isNotEmpty()
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

