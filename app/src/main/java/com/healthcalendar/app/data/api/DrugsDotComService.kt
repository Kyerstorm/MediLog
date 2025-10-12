package com.healthcalendar.app.data.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for searching and accessing Drugs.com medication information.
 * Uses a local JSON index for fast autocomplete, opens browser to Drugs.com for details.
 */
@Singleton
class DrugsDotComService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val medications: List<DrugsMedicationInfo> by lazy {
        loadMedicationsFromAssets()
    }
    
    /**
     * Get medication suggestions based on search query
     */
    fun getSuggestions(query: String, limit: Int = 10): List<String> {
        if (query.isBlank()) return emptyList()
        
        return medications
            .filter { it.name.contains(query, ignoreCase = true) }
            .sortedBy { 
                // Prioritize exact matches at start of name
                when {
                    it.name.startsWith(query, ignoreCase = true) -> 0
                    else -> 1
                }
            }
            .take(limit)
            .map { it.name }
    }
    
    /**
     * Get the Drugs.com URL for a specific medication
     */
    fun getMedicationUrl(name: String): String? {
        val medication = medications.find { 
            it.name.equals(name, ignoreCase = true) 
        }
        return medication?.url ?: buildGenericUrl(name)
    }
    
    /**
     * Build generic Drugs.com URL from medication name
     */
    private fun buildGenericUrl(name: String): String {
        // Convert name to URL format (lowercase, spaces to hyphens)
        val urlName = name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "") // Remove special chars
            .replace(Regex("\\s+"), "-") // Spaces to hyphens
        
        return "https://www.drugs.com/$urlName.html"
    }
    
    /**
     * Get search URL for medications not in index
     */
    fun getSearchUrl(query: String): String {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        return "https://www.drugs.com/search.php?searchterm=$encodedQuery"
    }
    
    /**
     * Load medications from local JSON index
     */
    private fun loadMedicationsFromAssets(): List<DrugsMedicationInfo> {
        return try {
            val jsonString = context.assets.open("drugs_medications.json")
                .bufferedReader()
                .use { it.readText() }
            
            val type = object : TypeToken<List<DrugsMedicationInfo>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("DrugsDotComService", "Error loading medications index", e)
            emptyList()
        }
    }
    
    /**
     * Get all available medication names (for debugging/testing)
     */
    fun getAllMedicationNames(): List<String> {
        return medications.map { it.name }.sorted()
    }
    
    /**
     * Check if medication exists in local index
     */
    fun isMedicationInIndex(name: String): Boolean {
        return medications.any { it.name.equals(name, ignoreCase = true) }
    }
}

/**
 * Data model for Drugs.com medication information
 */
data class DrugsMedicationInfo(
    val name: String,
    val url: String
)
