package com.healthcalendar.app.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.database.entities.Note
import com.healthcalendar.app.data.database.entities.NoteCategory
import com.healthcalendar.app.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    navController: NavController,
    noteId: Long? = null,
    viewModel: NoteViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(NoteCategory.GENERAL) }
    var isPinned by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val isEditMode = noteId != null
    
    // Load note if editing
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNoteById(noteId)?.let { note ->
                title = note.title
                content = note.content
                category = note.category
                isPinned = note.isPinned
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Note" else "Add Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isPinned = !isPinned }
                    ) {
                        Icon(
                            if (isPinned) Icons.Filled.PushPin else Icons.Filled.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = category.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    NoteCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name.replace("_", " ")) },
                            onClick = {
                                category = cat
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Content field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )
            
            // Save button
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            val now = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                            
                            if (isEditMode && noteId != null) {
                                viewModel.getNoteById(noteId)?.let { existingNote ->
                                    viewModel.updateNote(
                                        existingNote.copy(
                                            title = title,
                                            content = content,
                                            category = category,
                                            isPinned = isPinned,
                                            updatedAt = now
                                        )
                                    )
                                }
                            } else {
                                viewModel.insertNote(
                                    Note(
                                        title = title,
                                        content = content,
                                        category = category,
                                        isPinned = isPinned,
                                        createdAt = now,
                                        updatedAt = now
                                    )
                                )
                            }
                            isLoading = false
                            navController.navigateUp()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && title.isNotBlank() && content.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Note" else "Save Note")
                }
            }
        }
    }
}
