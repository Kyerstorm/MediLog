package com.healthcalendar.app.data.api

/**
 * NHS medication information data model (lightweight - only name and URL)
 */
data class NHSMedicationInfo(
    val name: String,
    val url: String,
    // Deprecated fields - kept for backward compatibility but not used
    @Deprecated("No longer stored locally - open URL in browser instead")
    val genericName: String? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val brandNames: List<String>? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val description: String = "",
    @Deprecated("No longer stored locally - open URL in browser instead")
    val dosageInfo: String = "",
    @Deprecated("No longer stored locally - open URL in browser instead")
    val sideEffects: List<String> = emptyList(),
    @Deprecated("No longer stored locally - open URL in browser instead")
    val warnings: List<String> = emptyList(),
    @Deprecated("No longer stored locally - open URL in browser instead")
    val keyFacts: List<String>? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val howItWorks: String? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val whoCanTake: List<String>? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val commonSideEffects: List<String>? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val seriousSideEffects: List<String>? = null,
    @Deprecated("No longer stored locally - open URL in browser instead")
    val sideEffectsSummary: String? = null
)

/**
 * Interface for NHS medication data source
 * Can be implemented with local JSON database or web scraping
 */
interface NHSMedicationService {
    
    /**
     * Search for medications by name
     */
    suspend fun searchMedication(query: String): List<NHSMedicationInfo>
    
    /**
     * Get detailed information about a specific medication
     */
    suspend fun getMedicationDetails(medicationName: String): NHSMedicationInfo?
    
    /**
     * Get search suggestions based on partial query
     */
    suspend fun getSuggestions(query: String): List<String>
}

/**
 * Example implementation (to be completed):
 * 
 * class NHSMedicationServiceImpl @Inject constructor() : NHSMedicationService {
 *     
 *     override suspend fun searchMedication(query: String): List<NHSMedicationInfo> {
 *         return withContext(Dispatchers.IO) {
 *             try {
 *                 // Web scraping approach
 *                 val doc = Jsoup.connect("https://www.nhs.uk/medicines/$query/").get()
 *                 
 *                 // Parse medication info from HTML
 *                 val name = doc.select(".medication-name").text()
 *                 val description = doc.select(".medication-description").text()
 *                 // ... extract more fields
 *                 
 *                 listOf(NHSMedicationInfo(...))
 *             } catch (e: Exception) {
 *                 emptyList()
 *             }
 *         }
 *     }
 * }
 */
