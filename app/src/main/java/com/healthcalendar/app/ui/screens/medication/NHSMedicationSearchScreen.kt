package com.healthcalendar.app.ui.screens.medication

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.healthcalendar.app.data.api.NHSMedicationInfo
import com.healthcalendar.app.data.database.entities.FavoriteSortOrder
import com.healthcalendar.app.viewmodel.DocumentScannerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NHSMedicationSearchScreen(
    navController: NavController,
    viewModel: DocumentScannerViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.nhsSearchResults.collectAsState()
    val suggestions by viewModel.nhsSuggestions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val favorites by viewModel.nhsFavorites.collectAsState()
    val recentlyViewed by viewModel.nhsRecentlyViewed.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val favoriteSortOrder by viewModel.favoriteSortOrder.collectAsState()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<NHSMedicationInfo?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NHS Medication Database") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Network connectivity indicator
                    if (!isOnline) {
                        IconButton(onClick = { 
                            // Show snackbar or toast about offline status
                        }) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = "Offline - No internet connection",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.getNHSSuggestions(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search NHS medications...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearNHSSearch()
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            // Search button
            Button(
                onClick = { viewModel.searchNHSMedications(searchQuery) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = searchQuery.length >= 2
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search NHS Database")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            when (uiState) {
                is DocumentScannerViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is DocumentScannerViewModel.UiState.Empty -> {
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = (uiState as DocumentScannerViewModel.UiState.Empty).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is DocumentScannerViewModel.UiState.Error -> {
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
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (uiState as DocumentScannerViewModel.UiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Make sure you have an internet connection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Recently Viewed Section
                        if (recentlyViewed.isNotEmpty() && searchResults.isEmpty()) {
                            item {
                                Text(
                                    text = "Recently Viewed",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(recentlyViewed) { bookmark ->
                                        RecentlyViewedCard(
                                            bookmark = bookmark,
                                            onClick = {
                                                selectedMedication = NHSMedicationInfo(
                                                    name = bookmark.medicationName,
                                                    url = bookmark.nhsUrl
                                                )
                                                showBottomSheet = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Favorites Section
                        if (favorites.isNotEmpty() && searchResults.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Favorites",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    // Sort menu button
                                    Box {
                                        IconButton(onClick = { showSortMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Filled.Sort,
                                                contentDescription = "Sort favorites"
                                            )
                                        }
                                        
                                        DropdownMenu(
                                            expanded = showSortMenu,
                                            onDismissRequest = { showSortMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        if (favoriteSortOrder == FavoriteSortOrder.RECENTLY_ADDED) {
                                                            Icon(
                                                                Icons.Filled.Check,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                        Text("Recently Added")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.setFavoriteSortOrder(FavoriteSortOrder.RECENTLY_ADDED)
                                                    showSortMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        if (favoriteSortOrder == FavoriteSortOrder.ALPHABETICAL) {
                                                            Icon(
                                                                Icons.Filled.Check,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                        Text("Alphabetical")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.setFavoriteSortOrder(FavoriteSortOrder.ALPHABETICAL)
                                                    showSortMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        if (favoriteSortOrder == FavoriteSortOrder.MOST_VIEWED) {
                                                            Icon(
                                                                Icons.Filled.Check,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                        Text("Most Viewed")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.setFavoriteSortOrder(FavoriteSortOrder.MOST_VIEWED)
                                                    showSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            items(favorites) { bookmark ->
                                NHSMedicationCard(
                                    medication = NHSMedicationInfo(
                                        name = bookmark.medicationName,
                                        url = bookmark.nhsUrl
                                    ),
                                    viewCount = bookmark.viewCount,
                                    isFavorite = true,
                                    onClick = {
                                        selectedMedication = NHSMedicationInfo(
                                            name = bookmark.medicationName,
                                            url = bookmark.nhsUrl
                                        )
                                        showBottomSheet = true
                                    }
                                )
                            }
                        }
                        
                        // Show suggestions or results
                        if (suggestions.isNotEmpty() && searchResults.isEmpty()) {
                            item {
                                Text(
                                    text = "Suggestions",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }
                            items(suggestions) { suggestion ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = suggestion
                                            viewModel.searchNHSMedications(suggestion)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = suggestion)
                                    }
                                }
                            }
                        } else if (searchResults.isNotEmpty()) {
                            item {
                                Text(
                                    text = "${searchResults.size} results found",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(searchResults) { medication ->
                                val isFavorite = favorites.any { it.medicationName == medication.name }
                                val viewCount = recentlyViewed.find { it.medicationName == medication.name }?.viewCount ?: 0
                                
                                NHSMedicationCard(
                                    medication = medication,
                                    viewCount = viewCount,
                                    isFavorite = isFavorite,
                                    onClick = {
                                        selectedMedication = medication
                                        showBottomSheet = true
                                    }
                                )
                            }
                        } else if (favorites.isEmpty() && recentlyViewed.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Search the NHS medication database",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Get information about medications, dosages, and side effects",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Medication Preview Bottom Sheet
        if (showBottomSheet && selectedMedication != null) {
            MedicationPreviewBottomSheet(
                medication = selectedMedication!!,
                sheetState = sheetState,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                },
                onViewOnNHS = {
                    if (isOnline) {
                        viewModel.recordNHSMedicationView(selectedMedication!!)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedMedication!!.url))
                        context.startActivity(intent)
                    } else {
                        // Show offline warning - medication will be recorded as viewed when online
                        android.widget.Toast.makeText(
                            context,
                            "No internet connection. Please connect to view NHS website.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                onToggleFavorite = {
                    viewModel.toggleNHSFavorite(selectedMedication!!)
                },
                onSaveToMyMedications = { dosage, unit ->
                    // Navigate to Add Medication screen with pre-filled data
                    navController.navigate(
                        com.healthcalendar.app.ui.navigation.Screen.AddMedication.createRoute(
                            name = selectedMedication!!.name,
                            dosage = dosage,
                            unit = unit
                        )
                    )
                    // Dismiss the bottom sheet
                    showBottomSheet = false
                    selectedMedication = null
                },
                isOnline = isOnline,
                isFavorite = favorites.any { it.medicationName == selectedMedication!!.name },
                viewCount = recentlyViewed.find { it.medicationName == selectedMedication!!.name }?.viewCount ?: 0
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NHSMedicationCard(
    medication: NHSMedicationInfo,
    viewCount: Int = 0,
    isFavorite: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$viewCount views",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Filled.OpenInBrowser,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentlyViewedCard(
    bookmark: com.healthcalendar.app.data.database.entities.NHSMedicationBookmark,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = bookmark.medicationName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${bookmark.viewCount} views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationPreviewBottomSheet(
    medication: NHSMedicationInfo,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onViewOnNHS: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSaveToMyMedications: (dosage: String, unit: String) -> Unit,
    isOnline: Boolean,
    isFavorite: Boolean,
    viewCount: Int
) {
    var dosage by remember { mutableStateOf("") }
    var dosageUnit by remember { mutableStateOf("mg") }
    var showSaveDialog by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // View count
            if (viewCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Viewed $viewCount ${if (viewCount == 1) "time" else "times"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // NHS URL
            Text(
                text = "NHS Website",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = medication.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Offline warning
            if (!isOnline) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
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
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "No internet connection. Connect to view NHS website.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action buttons
            Button(
                onClick = onViewOnNHS,
                modifier = Modifier.fillMaxWidth(),
                enabled = isOnline
            ) {
                Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Full Details on NHS Website")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { showSaveDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to My Medications")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Save to My Medications Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Add to My Medications") },
            text = {
                Column {
                    Text("Enter dosage information:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text("Dosage") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dosageUnit,
                            onValueChange = { dosageUnit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveToMyMedications(dosage, dosageUnit)
                        showSaveDialog = false
                    },
                    enabled = dosage.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
