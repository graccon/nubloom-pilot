package com.sujin.nubloompilot.pages

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.repository.SleepResultRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class SleepPageUiState(
    val latestSummary: DailyHealthSummary? = null,
    val recentSummaries: List<DailyHealthSummary> = emptyList(),
    val sleepSummariesLast24h: List<DailyHealthSummary> = emptyList(),
    val checkInHistory: List<SleepResult> = emptyList(),
    val averageSleepDurationMinutes: Long? = null,
    val averageShiftSleepDurationMinutes: Long? = null,
    val todayShift: String? = null,
    val averageWakeHeartRate: Long? = null,
    val isLoading: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val isBaselineLoading: Boolean = false,
    val errorMessage: String? = null
)

class SleepViewModel(
    private val healthSummaryRepository: HealthSummaryRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val shiftScheduleRepository: ShiftScheduleRepository,
    private val sleepResultRepository: SleepResultRepository
) : ViewModel() {

    class Factory(
        private val healthSummaryRepository: HealthSummaryRepository,
        private val healthConnectRepository: HealthConnectRepository,
        private val shiftScheduleRepository: ShiftScheduleRepository,
        private val sleepResultRepository: SleepResultRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(
                healthSummaryRepository,
                healthConnectRepository,
                shiftScheduleRepository,
                sleepResultRepository
            ) as T
        }
    }

    var uiState by mutableStateOf(SleepPageUiState())
        private set

    private var hasLoadedOnce = false

    fun loadSleepData() {
        if (hasLoadedOnce && uiState.latestSummary != null) return

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d("SleepLoad", "loadSleepData start")
            
            uiState = uiState.copy(
                isLoading = true, 
                isHistoryLoading = true, 
                isBaselineLoading = true,
                errorMessage = null
            )
            
            // 0. Load from Cache first
            try {
                val cached = sleepResultRepository.getCachedLatestSummary()
                if (cached != null) {
                    val cachedDate = cached.sleepEndTime.atZone(ZoneId.systemDefault()).toLocalDate()
                    if (cachedDate == LocalDate.now()) {
                        uiState = uiState.copy(latestSummary = cached, isLoading = false)
                        Log.d("SleepLoad", "cached latestSummary applied")
                    } else {
                        Log.d("SleepLoad", "cached latestSummary ignored (date mismatch: $cachedDate)")
                    }
                } else {
                    Log.d("SleepLoad", "cached latestSummary not found")
                }
            } catch (e: Exception) {
                Log.e("SleepLoad", "Failed to load latestSummary cache", e)
            }

            try {
                if (healthConnectRepository.isHealthConnectAvailable() && healthConnectRepository.hasHealthPermissions()) {
                    
                    // 1. Essential Data (Latest Summary & 24h) - Load these first to show UI
                    coroutineScope {
                        val latestSummaryDeferred = async { healthSummaryRepository.getLatestHealthSummary() }
                        val summaries24hDeferred = async { healthSummaryRepository.getHealthSummariesLast24h() }
                        
                        val latestSummary = latestSummaryDeferred.await()
                        val summaries24h = summaries24hDeferred.await()
                        
                        val essentialTime = System.currentTimeMillis() - startTime
                        Log.d("SleepLoad", "Essential data loaded in ${essentialTime}ms")

                        // Update Cache
                        latestSummary?.let {
                            sleepResultRepository.saveLatestSummaryCache(it)
                            Log.d("SleepLoad", "latestSummary cache updated")
                        }

                        uiState = uiState.copy(
                            latestSummary = latestSummary,
                            sleepSummariesLast24h = summaries24h,
                            isLoading = false // Release full screen loading
                        )
                    }

                    // 2. Secondary Data (Firestore & Baselines) - Run in background
                    
                    // Firestore History
                    launch {
                        val histStartTime = System.currentTimeMillis()
                        try {
                            val history = sleepResultRepository.getSleepResultsInDateRange(days = 30)
                            uiState = uiState.copy(checkInHistory = history, isHistoryLoading = false)
                            Log.d("SleepLoad", "Firestore history loaded in ${System.currentTimeMillis() - histStartTime}ms")
                        } catch (e: Exception) {
                            uiState = uiState.copy(isHistoryLoading = false)
                        }
                    }

                    // HC Baselines (Heavy)
                    launch {
                        val baseStartTime = System.currentTimeMillis()
                        try {
                            // Reuse 20-day summary to derive both 4-day baseline and shift-specific baseline
                            val manyRecentSummaries = healthSummaryRepository.getRecentHealthSummaries(limit = 20)
                            
                            val recentSummaries4 = manyRecentSummaries.take(4)
                            val avgSleepDur = healthSummaryRepository.getBaselineSleepDurationMinutes(recentSummaries4)
                            val avgWakeHR = healthSummaryRepository.getBaselineWakeHeartRate(recentSummaries4)

                            val today = LocalDate.now()
                            val todayShift = shiftScheduleRepository.getShiftForDate(today)
                            var avgShiftSleepDur: Long? = null

                            if (!todayShift.isNullOrBlank()) {
                                val pastShiftSummaries = manyRecentSummaries.filter { summary ->
                                    val summaryDate = summary.sleepEndTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                    val shiftOnThatDate = shiftScheduleRepository.getShiftForDate(summaryDate)
                                    shiftOnThatDate == todayShift && summaryDate != today
                                }

                                if (pastShiftSummaries.size >= 3) {
                                    avgShiftSleepDur = pastShiftSummaries.take(3)
                                        .map { it.sleepDurationMinutes }
                                        .average()
                                        .toLong()
                                }
                            }

                            uiState = uiState.copy(
                                recentSummaries = recentSummaries4,
                                averageSleepDurationMinutes = avgSleepDur,
                                averageWakeHeartRate = avgWakeHR,
                                todayShift = todayShift,
                                averageShiftSleepDurationMinutes = avgShiftSleepDur,
                                isBaselineLoading = false
                            )
                            Log.d("SleepLoad", "HC Baselines loaded in ${System.currentTimeMillis() - baseStartTime}ms")
                        } catch (e: Exception) {
                            uiState = uiState.copy(isBaselineLoading = false)
                        }
                    }
                    
                    hasLoadedOnce = true
                }
            } catch (e: Exception) {
                Log.e("SleepLoad", "Error in loadSleepData", e)
                uiState = uiState.copy(errorMessage = "데이터를 불러오는 중 오류가 발생했습니다.", isLoading = false)
            }
        }
    }
}
