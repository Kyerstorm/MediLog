package com.healthcalendar.app.data.api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNHSMedicationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : NHSMedicationService {
    
    companion object {
        private const val TAG = "LocalNHSMedService"
        private const val MEDICATIONS_FILE = "nhs_medications.json"
    }
    
    private var medicationsCache: List<NHSMedicationInfo>? = null
    
    /**
     * Load medications from JSON file
     */
    private suspend fun loadMedications(): List<NHSMedicationInfo> {
        if (medicationsCache != null) {
            return medicationsCache!!
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open(MEDICATIONS_FILE)
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<NHSMedicationInfo>>() {}.type
                val medications: List<NHSMedicationInfo> = gson.fromJson(reader, type)
                reader.close()
                
                medicationsCache = medications
                Log.d(TAG, "Loaded ${medications.size} medications from local database")
                medications
            } catch (e: Exception) {
                Log.e(TAG, "Error loading medications from assets", e)
                emptyList()
            }
        }
    }
    
    override suspend fun searchMedication(query: String): List<NHSMedicationInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val allMedications = loadMedications()
                
                if (query.isBlank()) {
                    return@withContext allMedications.take(10) // Return first 10 if no query
                }
                
                // Search by name only (lightweight index)
                val results = allMedications.filter { medication ->
                    medication.name.contains(query, ignoreCase = true)
                }
                
                Log.d(TAG, "Search '$query' found ${results.size} results")
                results
            } catch (e: Exception) {
                Log.e(TAG, "Error searching medications", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getMedicationDetails(medicationName: String): NHSMedicationInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val allMedications = loadMedications()
                
                // Find exact match or close match by name only
                allMedications.find { medication ->
                    medication.name.equals(medicationName, ignoreCase = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting medication details", e)
                null
            }
        }
    }
    
    override suspend fun getSuggestions(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.length < 2) {
                    return@withContext emptyList()
                }
                
                val allMedications = loadMedications()
                val suggestions = mutableSetOf<String>()
                
                allMedications.forEach { medication ->
                    // Add name if it matches
                    if (medication.name.contains(query, ignoreCase = true)) {
                        suggestions.add(medication.name)
                    }
                    
                    // Add generic name if it matches
                    medication.genericName?.let { genericName ->
                        if (genericName.contains(query, ignoreCase = true)) {
                            suggestions.add(genericName)
                        }
                    }
                    
                    // Add brand names if they match
                    medication.brandNames?.forEach { brandName ->
                        if (brandName.contains(query, ignoreCase = true)) {
                            suggestions.add(brandName)
                        }
                    }
                }
                
                suggestions.take(5).toList()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting suggestions", e)
                emptyList()
            }
        }
    }
}
