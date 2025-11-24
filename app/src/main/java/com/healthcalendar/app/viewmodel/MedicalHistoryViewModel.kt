package com.healthcalendar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.database.entities.MedicationLog
import com.healthcalendar.app.data.database.entities.MedicationStatus
import com.healthcalendar.app.data.repository.MedicationLogRepository
import com.healthcalendar.app.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MedicalHistoryViewModel @Inject constructor(
    private val medicationLogRepository: MedicationLogRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {
    
    private val _statistics = MutableStateFlow<MedicalHistoryStatistics>(MedicalHistoryStatistics())
    val statistics: StateFlow<MedicalHistoryStatistics> = _statistics.asStateFlow()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val today = now.date
                val sevenDaysAgo = today.minus(7, DateTimeUnit.DAY)
                val thirtyDaysAgo = today.minus(30, DateTimeUnit.DAY)

                // Get only logs from last 30 days using SQL-level filtering (optimized for performance)
                val startDateTime = LocalDateTime(thirtyDaysAgo, LocalTime(0, 0))
                val endDateTime = LocalDateTime(today, LocalTime(23, 59, 59))
                val allLogs = medicationLogRepository.getLogsBetweenDatesSync(startDateTime, endDateTime)
                
                // If no logs at all, return empty statistics
                if (allLogs.isEmpty()) {
                    val activeMedications = medicationRepository.getAllMedications().first()
                        .filter { it.isActive }
                    
                    _statistics.value = MedicalHistoryStatistics(
                        totalActiveMedications = activeMedications.size,
                        todayAdherence = 0f,
                        weeklyAdherence = 0f,
                        monthlyAdherence = 0f,
                        currentStreak = 0,
                        perMedicationStats = emptyList(),
                        recentLogs = emptyList(),
                        adherenceData = emptyList()
                    )
                    _uiState.value = UiState.Success
                    return@launch
                }
                
                // Calculate today's adherence
                val todayLogs = allLogs.filter { it.scheduledTime.date == today }
                val todayAdherence = calculateAdherence(todayLogs)
                
                // Calculate weekly adherence
                val weekLogs = allLogs.filter { 
                    it.scheduledTime.date >= sevenDaysAgo && it.scheduledTime.date <= today 
                }
                val weeklyAdherence = calculateAdherence(weekLogs)
                
                // Calculate monthly adherence
                val monthLogs = allLogs // Already filtered to last 30 days
                val monthlyAdherence = calculateAdherence(monthLogs)
                
                // Calculate streak (limited to data we have)
                val streak = calculateStreak(allLogs, today)
                
                // Get active medication count
                val activeMedications = medicationRepository.getAllMedications().first()
                    .filter { it.isActive }
                
                // Calculate per-medication stats (only for last 30 days)
                val perMedicationStats = activeMedications.mapNotNull { medication ->
                    val medLogs = allLogs.filter { it.medicationId == medication.id }
                    if (medLogs.isEmpty()) return@mapNotNull null
                    
                    val taken = medLogs.count { it.status == MedicationStatus.TAKEN }
                    val scheduled = medLogs.size
                    val adherenceRate = if (scheduled > 0) (taken * 100f / scheduled) else 0f
                    
                    PerMedicationStat(
                        medicationId = medication.id,
                        medicationName = medication.name,
                        totalScheduled = scheduled,
                        totalTaken = taken,
                        adherenceRate = adherenceRate
                    )
                }
                
                // Get recent logs (last 7 days, limit to 50 most recent)
                val recentLogs = weekLogs.sortedByDescending { it.scheduledTime }.take(50)
                
                // Calculate adherence data for chart (last 7 days)
                val adherenceData = (0..6).map { daysAgo ->
                    val date = today.minus(daysAgo, DateTimeUnit.DAY)
                    val dayLogs = allLogs.filter { it.scheduledTime.date == date }
                    AdherenceDataPoint(
                        date = date,
                        adherence = calculateAdherence(dayLogs)
                    )
                }.reversed()
                
                _statistics.value = MedicalHistoryStatistics(
                    totalActiveMedications = activeMedications.size,
                    todayAdherence = todayAdherence,
                    weeklyAdherence = weeklyAdherence,
                    monthlyAdherence = monthlyAdherence,
                    currentStreak = streak,
                    perMedicationStats = perMedicationStats,
                    recentLogs = recentLogs,
                    adherenceData = adherenceData
                )
                
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun calculateAdherence(logs: List<MedicationLog>): Float {
        if (logs.isEmpty()) return 0f
        val taken = logs.count { it.status == MedicationStatus.TAKEN }
        return (taken * 100f / logs.size)
    }
    
    private fun calculateStreak(logs: List<MedicationLog>, today: LocalDate): Int {
        if (logs.isEmpty()) return 0
        
        var streak = 0
        var currentDate = today
        var consecutiveDaysWithoutData = 0
        
        // Go backwards day by day
        for (i in 0 until 365) { // Hard limit of 365 iterations
            val dayLogs = logs.filter { it.scheduledTime.date == currentDate }
            
            if (dayLogs.isEmpty()) {
                // No medications scheduled for this day
                consecutiveDaysWithoutData++
                // If we've gone 3 days without any data, assume streak is broken
                if (consecutiveDaysWithoutData >= 3) {
                    break
                }
                currentDate = currentDate.minus(1, DateTimeUnit.DAY)
                continue
            }
            
            // Reset counter when we find data
            consecutiveDaysWithoutData = 0
            
            val dayAdherence = calculateAdherence(dayLogs)
            if (dayAdherence >= 80f) { // 80% or more = good day
                streak++
                currentDate = currentDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        
        return streak
    }
    
    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}

data class MedicalHistoryStatistics(
    val totalActiveMedications: Int = 0,
    val todayAdherence: Float = 0f,
    val weeklyAdherence: Float = 0f,
    val monthlyAdherence: Float = 0f,
    val currentStreak: Int = 0,
    val perMedicationStats: List<PerMedicationStat> = emptyList(),
    val recentLogs: List<MedicationLog> = emptyList(),
    val adherenceData: List<AdherenceDataPoint> = emptyList()
)

data class PerMedicationStat(
    val medicationId: Long,
    val medicationName: String,
    val totalScheduled: Int,
    val totalTaken: Int,
    val adherenceRate: Float
)

data class AdherenceDataPoint(
    val date: LocalDate,
    val adherence: Float
)
