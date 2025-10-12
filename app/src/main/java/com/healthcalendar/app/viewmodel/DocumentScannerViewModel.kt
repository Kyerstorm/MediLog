package com.healthcalendar.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.api.NHSMedicationInfo
import com.healthcalendar.app.data.database.entities.ScannedDocumentEntity
import com.healthcalendar.app.data.database.entities.DocumentType
import com.healthcalendar.app.data.database.entities.NHSMedicationBookmark
import com.healthcalendar.app.data.database.entities.FavoriteSortOrder
import com.healthcalendar.app.data.repository.NHSMedicationRepository
import com.healthcalendar.app.data.repository.NHSMedicationBookmarkRepository
import com.healthcalendar.app.data.repository.ScannedDocumentRepository
import com.healthcalendar.app.util.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class DocumentScannerViewModel @Inject constructor(
    private val scannedDocumentRepository: ScannedDocumentRepository,
    private val nhsMedicationRepository: NHSMedicationRepository,
    private val nhsBookmarkRepository: NHSMedicationBookmarkRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    // Scanned Documents
    val allDocuments: StateFlow<List<ScannedDocumentEntity>> = 
        scannedDocumentRepository.getAllDocuments()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    private val _selectedDocument = MutableStateFlow<ScannedDocumentEntity?>(null)
    val selectedDocument: StateFlow<ScannedDocumentEntity?> = _selectedDocument.asStateFlow()
    
    // NHS Medication Search
    private val _nhsSearchResults = MutableStateFlow<List<NHSMedicationInfo>>(emptyList())
    val nhsSearchResults: StateFlow<List<NHSMedicationInfo>> = _nhsSearchResults.asStateFlow()
    
    private val _selectedNHSMedication = MutableStateFlow<NHSMedicationInfo?>(null)
    val selectedNHSMedication: StateFlow<NHSMedicationInfo?> = _selectedNHSMedication.asStateFlow()
    
    private val _nhsSuggestions = MutableStateFlow<List<String>>(emptyList())
    val nhsSuggestions: StateFlow<List<String>> = _nhsSuggestions.asStateFlow()
    
    // NHS Medication Bookmarks
    private val _favoriteSortOrder = MutableStateFlow(FavoriteSortOrder.RECENTLY_ADDED)
    val favoriteSortOrder: StateFlow<FavoriteSortOrder> = _favoriteSortOrder.asStateFlow()
    
    val nhsFavorites: StateFlow<List<NHSMedicationBookmark>> = 
        _favoriteSortOrder.flatMapLatest { sortOrder ->
            nhsBookmarkRepository.getFavorites(sortOrder)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val nhsRecentlyViewed: StateFlow<List<NHSMedicationBookmark>> = 
        nhsBookmarkRepository.getRecentlyViewed()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    // Network State
    val isOnline: StateFlow<Boolean> = 
        networkMonitor.observeNetworkState()
            .map { state -> 
                state is NetworkMonitor.NetworkState.Available
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = networkMonitor.isOnline()
            )
    
    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.Idle)
    val scanningState: StateFlow<ScanningState> = _scanningState.asStateFlow()
    
    // Document Management
    
    fun saveScannedDocument(
        title: String,
        description: String,
        fileUri: Uri,
        documentType: DocumentType,
        medicationId: Long? = null,
        appointmentId: Long? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                _scanningState.value = ScanningState.Saving
                
                val document = ScannedDocumentEntity(
                    title = title,
                    description = description,
                    fileUri = fileUri.toString(),
                    mimeType = "", // Will be determined by viewer
                    fileSize = 0, // Unknown for scanned documents
                    documentType = documentType,
                    medicationId = medicationId,
                    appointmentId = appointmentId,
                    scannedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    tags = tags
                )
                
                scannedDocumentRepository.insertDocument(document)
                _scanningState.value = ScanningState.Success
                
            } catch (e: Exception) {
                _scanningState.value = ScanningState.Error(e.message ?: "Failed to save document")
            }
        }
    }
    
    fun saveUploadedDocument(
        title: String,
        description: String,
        fileUri: Uri,
        documentType: DocumentType,
        mimeType: String,
        fileSize: Long,
        medicationId: Long? = null,
        appointmentId: Long? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                _scanningState.value = ScanningState.Saving
                
                val document = ScannedDocumentEntity(
                    title = title,
                    description = description,
                    fileUri = fileUri.toString(),
                    mimeType = mimeType,
                    fileSize = fileSize,
                    documentType = documentType,
                    medicationId = medicationId,
                    appointmentId = appointmentId,
                    scannedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    tags = tags
                )
                
                scannedDocumentRepository.insertDocument(document)
                _scanningState.value = ScanningState.Success
                
            } catch (e: Exception) {
                _scanningState.value = ScanningState.Error(e.message ?: "Failed to upload document")
            }
        }
    }
    
    fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            try {
                val document = scannedDocumentRepository.getDocumentById(documentId)
                _selectedDocument.value = document
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load document")
            }
        }
    }
    
    fun getDocumentsForMedication(medicationId: Long): Flow<List<ScannedDocumentEntity>> {
        return scannedDocumentRepository.getDocumentsForMedication(medicationId)
    }
    
    fun deleteDocument(document: ScannedDocumentEntity) {
        viewModelScope.launch {
            try {
                scannedDocumentRepository.deleteDocument(document)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete document")
            }
        }
    }
    
    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            try {
                val document = scannedDocumentRepository.getDocumentById(documentId)
                if (document != null) {
                    scannedDocumentRepository.deleteDocument(document)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete document")
            }
        }
    }
    
    fun togglePin(documentId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            try {
                scannedDocumentRepository.togglePin(documentId, !isPinned)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to toggle pin")
            }
        }
    }
    
    fun updateDocument(document: ScannedDocumentEntity) {
        viewModelScope.launch {
            try {
                scannedDocumentRepository.updateDocument(document)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update document")
            }
        }
    }
    
    // NHS Medication Search
    
    fun searchNHSMedications(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                nhsMedicationRepository.searchMedications(query).collect { results ->
                    _nhsSearchResults.value = results
                    _uiState.value = if (results.isEmpty()) {
                        UiState.Empty("No medications found")
                    } else {
                        UiState.Success
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to search NHS database")
                _nhsSearchResults.value = emptyList()
            }
        }
    }
    
    fun getNHSMedicationDetails(medicationName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                nhsMedicationRepository.getMedicationDetails(medicationName).collect { details ->
                    _selectedNHSMedication.value = details
                    _uiState.value = if (details == null) {
                        UiState.Empty("No details found")
                    } else {
                        UiState.Success
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to get medication details")
            }
        }
    }
    
    fun getNHSSuggestions(query: String) {
        if (query.length < 2) {
            _nhsSuggestions.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                nhsMedicationRepository.getSuggestions(query).collect { suggestions ->
                    _nhsSuggestions.value = suggestions
                }
            } catch (e: Exception) {
                _nhsSuggestions.value = emptyList()
            }
        }
    }
    
    fun clearNHSSearch() {
        _nhsSearchResults.value = emptyList()
        _selectedNHSMedication.value = null
        _nhsSuggestions.value = emptyList()
    }
    
    // NHS Medication Bookmark Functions
    
    fun setFavoriteSortOrder(sortOrder: FavoriteSortOrder) {
        _favoriteSortOrder.value = sortOrder
    }
    
    fun toggleNHSFavorite(medication: NHSMedicationInfo) {
        viewModelScope.launch {
            try {
                nhsBookmarkRepository.toggleFavorite(medication.name, medication.url)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to toggle favorite")
            }
        }
    }
    
    fun recordNHSMedicationView(medication: NHSMedicationInfo) {
        viewModelScope.launch {
            try {
                nhsBookmarkRepository.recordView(medication.name, medication.url)
            } catch (e: Exception) {
                // Silently fail for view recording
            }
        }
    }
    
    suspend fun isNHSMedicationFavorite(medicationName: String): Boolean {
        return try {
            nhsBookmarkRepository.isFavorite(medicationName)
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getNHSMedicationViewCount(medicationName: String): Int {
        return try {
            nhsBookmarkRepository.getViewCount(medicationName)
        } catch (e: Exception) {
            0
        }
    }
    
    fun resetScanningState() {
        _scanningState.value = ScanningState.Idle
    }
    
    fun setScanningError(message: String) {
        _scanningState.value = ScanningState.Error(message)
    }
    
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Empty(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
    
    sealed class ScanningState {
        object Idle : ScanningState()
        object Scanning : ScanningState()
        object Saving : ScanningState()
        object Success : ScanningState()
        data class Error(val message: String) : ScanningState()
    }
}
