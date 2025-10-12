package com.healthcalendar.app.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NHSMedicationServiceImpl @Inject constructor() : NHSMedicationService {
    
    companion object {
        private const val TAG = "NHSMedicationService"
        private const val NHS_BASE_URL = "https://www.nhs.uk/medicines/"
        private const val SEARCH_TIMEOUT = 10000 // 10 seconds
    }
    
    override suspend fun searchMedication(query: String): List<NHSMedicationInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val medications = mutableListOf<NHSMedicationInfo>()
                
                // Search the NHS medicines A-Z page
                val searchUrl = "${NHS_BASE_URL}?q=${query.replace(" ", "+")}"
                Log.d(TAG, "Searching NHS: $searchUrl")
                
                val doc: Document = Jsoup.connect(searchUrl)
                    .timeout(SEARCH_TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get()
                
                // Parse search results
                val results = doc.select("li.nhsuk-list-panel__item")
                
                results.forEach { element ->
                    try {
                        val link = element.select("a").first()
                        val name = link?.text()?.trim() ?: return@forEach
                        val url = link.attr("href")
                        
                        // Only add if it matches the search query
                        if (name.contains(query, ignoreCase = true)) {
                            medications.add(
                                NHSMedicationInfo(
                                    name = name,
                                    genericName = null,
                                    description = "Click for more details",
                                    dosageInfo = "",
                                    sideEffects = emptyList(),
                                    warnings = emptyList(),
                                    url = if (url.startsWith("http")) url else "https://www.nhs.uk$url"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing search result", e)
                    }
                }
                
                Log.d(TAG, "Found ${medications.size} medications")
                medications
                
            } catch (e: Exception) {
                Log.e(TAG, "Error searching NHS medications", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getMedicationDetails(medicationName: String): NHSMedicationInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // Convert medication name to URL format
                val urlName = medicationName.lowercase()
                    .replace(" ", "-")
                    .replace(Regex("[^a-z0-9-]"), "")
                
                val url = "$NHS_BASE_URL$urlName/"
                Log.d(TAG, "Fetching details from: $url")
                
                val doc: Document = Jsoup.connect(url)
                    .timeout(SEARCH_TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get()
                
                // Extract medication information
                val title = doc.select("h1").first()?.text() ?: medicationName
                
                // Get description (usually in the first paragraph)
                val description = doc.select("p.nhsuk-body-l").first()?.text()
                    ?: doc.select("p").first()?.text()
                    ?: "No description available"
                
                // Extract side effects
                val sideEffects = mutableListOf<String>()
                doc.select("section#side-effects li").forEach { li ->
                    sideEffects.add(li.text())
                }
                
                // Extract warnings
                val warnings = mutableListOf<String>()
                doc.select("section#cautions li").forEach { li ->
                    warnings.add(li.text())
                }
                
                // Extract dosage information
                val dosageInfo = doc.select("section#dosage p").text()
                    ?: doc.select("section#how-and-when-to-take p").text()
                    ?: ""
                
                // Look for generic name
                val genericName = doc.select("p:contains(generic name)").text()
                    .takeIf { it.isNotEmpty() }
                
                NHSMedicationInfo(
                    name = title,
                    genericName = genericName,
                    description = description,
                    dosageInfo = dosageInfo,
                    sideEffects = sideEffects,
                    warnings = warnings,
                    url = url
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching medication details", e)
                null
            }
        }
    }
    
    /**
     * Get medication suggestions as user types
     */
    override suspend fun getSuggestions(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.length < 2) return@withContext emptyList()
                
                val medications = searchMedication(query)
                medications.map { it.name }.take(10)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting suggestions", e)
                emptyList()
            }
        }
    }
}
