package com.healthcalendar.app.ui.screens.documents

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.healthcalendar.app.data.database.entities.DocumentType
import com.healthcalendar.app.viewmodel.DocumentScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    navController: NavController,
    documentId: Long,
    viewModel: DocumentScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allDocuments by viewModel.allDocuments.collectAsState()
    val document = remember(allDocuments, documentId) {
        allDocuments.find { it.id == documentId }
    }
    
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Helper functions to convert DocumentType <-> String
    fun documentTypeToString(type: DocumentType): String {
        return when (type) {
            DocumentType.PRESCRIPTION -> "Prescription"
            DocumentType.LAB_REPORT -> "Lab Report"
            DocumentType.MEDICAL_RECORD -> "Medical Record"
            DocumentType.INSURANCE -> "Insurance"
            DocumentType.MEDICATION_INFO -> "Vaccination"
            else -> "Other"
        }
    }
    
    // Edit dialog state
    var editTitle by remember { mutableStateOf(document?.title ?: "") }
    var editDescription by remember { mutableStateOf(document?.description ?: "") }
    var editType by remember { mutableStateOf(document?.let { documentTypeToString(it.documentType) } ?: "Other") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    // Update edit fields when document changes
    LaunchedEffect(document) {
        document?.let {
            editTitle = it.title
            editDescription = it.description
            editType = documentTypeToString(it.documentType)
        }
    }
    
    if (document == null) {
        // Document not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Document not found",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = { navController.navigateUp() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // Intentionally hide the title to reduce clutter in the viewer
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Pin button
                    IconButton(onClick = { 
                        viewModel.togglePin(document.id, document.isPinned)
                    }) {
                        // Always show a push-pin icon; only the tint changes depending on pinned state
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = if (document.isPinned) "Unpin" else "Pin",
                            tint = if (document.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Edit button
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            try {
                                val originalUri = Uri.parse(document.fileUri)

                                // Convert file:// URI to a content:// URI via FileProvider when necessary
                                val contentUri: Uri = if (originalUri.scheme == "file") {
                                    try {
                                        val path = originalUri.path.orEmpty()
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            File(path)
                                        )
                                    } catch (e: Exception) {
                                        // Fallback to original URI if conversion fails
                                        android.util.Log.w("DocumentViewer", "FileProvider conversion failed, using original URI", e)
                                        originalUri
                                    }
                                } else {
                                    originalUri
                                }

                                // Determine MIME type
                                val mimeType = document.mimeType.ifEmpty {
                                    when {
                                        document.fileUri.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                                        document.fileUri.endsWith(".doc", ignoreCase = true) ||
                                        document.fileUri.endsWith(".docx", ignoreCase = true) -> "application/msword"
                                        document.fileUri.endsWith(".jpg", ignoreCase = true) ||
                                        document.fileUri.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                                        document.fileUri.endsWith(".png", ignoreCase = true) -> "image/png"
                                        else -> "image/*"
                                    }
                                }

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = mimeType
                                    putExtra(Intent.EXTRA_STREAM, contentUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }

                                val chooser = Intent.createChooser(shareIntent, "Share document")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            } catch (e: SecurityException) {
                                android.util.Log.e("DocumentViewer", "Permission denied sharing document", e)
                                android.widget.Toast.makeText(context, "Permission denied. Cannot share this file.", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                android.util.Log.e("DocumentViewer", "Error sharing document", e)
                                android.widget.Toast.makeText(context, "Unable to share document. File may not be accessible.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }

                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Document info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (document.documentType) {
                                DocumentType.PRESCRIPTION -> Icons.Filled.Create
                                DocumentType.LAB_REPORT -> Icons.Filled.Info
                                DocumentType.MEDICAL_RECORD -> Icons.Filled.Favorite
                                else -> Icons.Filled.Info
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = document.documentType.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (document.description.isNotEmpty()) {
                        var showDescriptionDialog by remember { mutableStateOf(false) }
                        Text(
                            text = document.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showDescriptionDialog = true }
                        )
                        
                        if (showDescriptionDialog) {
                            AlertDialog(
                                onDismissRequest = { showDescriptionDialog = false },
                                title = { Text("Note") },
                                text = {
                                    Text(
                                        text = document.description,
                                        modifier = Modifier.verticalScroll(rememberScrollState())
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = { showDescriptionDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                    
                    Text(
                        text = "Scanned on ${document.scannedAt.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Document viewer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val fileUri = document.fileUri
                val mimeType = document.mimeType.ifEmpty {
                    when {
                        fileUri.endsWith(".pdf") -> "application/pdf"
                        fileUri.endsWith(".doc") || fileUri.endsWith(".docx") -> "application/msword"
                        else -> "image/*"
                    }
                }
                
                val isExternalDocument = mimeType.contains("pdf") || 
                                        mimeType.contains("word") || 
                                        mimeType.contains("msword") ||
                                        mimeType.contains("wordprocessingml") ||
                                        document.documentType == DocumentType.PDF_DOCUMENT ||
                                        document.documentType == DocumentType.WORD_DOCUMENT
                
                if (isExternalDocument) {
                    // PDF/Word document - show open in external app button
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                            Text(
                                text = when {
                                    mimeType.contains("pdf") -> "PDF Document"
                                    mimeType.contains("word") || mimeType.contains("msword") -> "Word Document"
                                    else -> "Document"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "Open in your preferred app",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            if (document.fileSize > 0) {
                                Text(
                                    text = "Size: ${String.format("%.2f", document.fileSize / 1024.0)} KB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Button(
                                onClick = {
                                    try {
                                        val originalUri = Uri.parse(fileUri)
                                        val contentUri: Uri = if (originalUri.scheme == "file") {
                                            try {
                                                val path = originalUri.path.orEmpty()
                                                FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    File(path)
                                                )
                                            } catch (e: Exception) {
                                                android.util.Log.w("DocumentViewer", "FileProvider conversion failed for open action", e)
                                                originalUri
                                            }
                                        } else {
                                            originalUri
                                        }

                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(contentUri, mimeType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle no app installed to open this file type or other errors
                                        android.util.Log.e("DocumentViewer", "Error opening external document", e)
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Document")
                            }
                        }
                    }
                } else {
                    // Image viewer with zoom and pan
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(fileUri)),
                        contentDescription = "Scanned document",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    
                                    if (scale > 1f) {
                                        val maxX = (size.width * (scale - 1)) / 2
                                        val maxY = (size.height * (scale - 1)) / 2
                                        offset = Offset(
                                            x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                            y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                        )
                                    } else {
                                        offset = Offset.Zero
                                    }
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                    
                    // Zoom indicator
                    if (scale > 1f) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            shape = MaterialTheme.shapes.small,
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "${(scale * 100).toInt()}%",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Reset zoom button
                    if (scale > 1f) {
                        FloatingActionButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset zoom")
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Delete Document?") },
            text = { Text("Are you sure you want to delete \"${document.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(document.id)
                        showDeleteDialog = false
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Edit document dialog
    if (showEditDialog) {
        val documentTypes = listOf("Prescription", "Lab Report", "Medical Record", "Insurance", "Vaccination", "Other")
        
        // Convert display string to document type enum
        fun stringToDocumentType(str: String): DocumentType {
            return when (str) {
                "Prescription" -> DocumentType.PRESCRIPTION
                "Lab Report" -> DocumentType.LAB_REPORT
                "Medical Record" -> DocumentType.MEDICAL_RECORD
                "Insurance" -> DocumentType.INSURANCE
                "Vaccination" -> DocumentType.MEDICATION_INFO
                else -> DocumentType.OTHER
            }
        }
        
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Document",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    
                    // Document Type Selector
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Document Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        documentTypes.forEach { type ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = {
                                    editType = type
                                },
                                color = if (type == editType) 
                                        MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = type,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (type == editType) 
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                document?.let {
                                    viewModel.updateDocument(
                                        it.copy(
                                            title = editTitle,
                                            description = editDescription,
                                            documentType = stringToDocumentType(editType)
                                        )
                                    )
                                }
                                showEditDialog = false
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

