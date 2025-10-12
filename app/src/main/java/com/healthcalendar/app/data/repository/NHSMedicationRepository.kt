package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.api.NHSMedicationInfo
import com.healthcalendar.app.data.api.NHSMedicationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NHSMedicationRepository @Inject constructor(
    private val nhsService: NHSMedicationService
) {
    
    /**
     * Search for medications on NHS website
     */
    fun searchMedications(query: String): Flow<List<NHSMedicationInfo>> = flow {
        val results = nhsService.searchMedication(query)
        emit(results)
    }
    
    /**
     * Get detailed information about a specific medication
     */
    fun getMedicationDetails(medicationName: String): Flow<NHSMedicationInfo?> = flow {
        val details = nhsService.getMedicationDetails(medicationName)
        emit(details)
    }
    
    /**
     * Get autocomplete suggestions
     */
    fun getSuggestions(query: String): Flow<List<String>> = flow {
        val suggestions = nhsService.getSuggestions(query)
        emit(suggestions)
    }
}
