package com.sujin.nubloompilot.pages

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sujin.nubloompilot.models.*
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.repository.SleepResultRepository
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.logic.RecoveryRhythmGenerator
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
    val shiftInsightSummary: ShiftInsightSummary? = null,
    val mctqBaselineProfile: MctqBaselineProfile? = null,
    val averageSleepDurationMinutes: Long? = null,
    val averageShiftSleepDurationMinutes: Long? = null,
    val todayShift: String? = null,
    val averageWakeHeartRate: Long? = null,
    val isLoading: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val isBaselineLoading: Boolean = false,
    val isShiftInsightLoading: Boolean = false,
    val errorMessage: String? = null
)

class SleepViewModel(
    private val healthSummaryRepository: HealthSummaryRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val shiftScheduleRepository: ShiftScheduleRepository,
    private val sleepResultRepository: SleepResultRepository,
    private val participantLocalStore: ParticipantLocalStore
) : ViewModel() {

    class Factory(
        private val healthSummaryRepository: HealthSummaryRepository,
        private val healthConnectRepository: HealthConnectRepository,
        private val shiftScheduleRepository: ShiftScheduleRepository,
        private val sleepResultRepository: SleepResultRepository,
        private val participantLocalStore: ParticipantLocalStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(
                healthSummaryRepository,
                healthConnectRepository,
                shiftScheduleRepository,
                sleepResultRepository,
                participantLocalStore
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
                isShiftInsightLoading = true,
                errorMessage = null
            )
            
            // 0. Load Cache & Baseline
            val mctqProfile = participantLocalStore.getBaselineProfile()
            uiState = uiState.copy(mctqBaselineProfile = mctqProfile)

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
                    
                    // Firestore History & Shift Insights
                    launch {
                        val histStartTime = System.currentTimeMillis()
                        try {
                            val history = sleepResultRepository.getSleepResultsInDateRange(days = 30)
                            
                            // Calculate Shift Insights from actual history
                            val insightSummary = calculateShiftInsights(history)
                            
                            // Generate Graph based on Insights & Baseline
                            val graphData = RecoveryRhythmGenerator().generate(
                                baselineProfile = uiState.mctqBaselineProfile,
                                shiftInsightSummary = insightSummary
                            )
                            
                            val finalInsight = insightSummary.copy(recoveryRhythmGraphData = graphData)

                            uiState = uiState.copy(
                                checkInHistory = history, 
                                isHistoryLoading = false,
                                shiftInsightSummary = finalInsight,
                                isShiftInsightLoading = false
                            )
                            Log.d("SleepLoad", "Firestore history & insights loaded in ${System.currentTimeMillis() - histStartTime}ms")
                        } catch (e: Exception) {
                            Log.e("SleepLoad", "Failed to load history insights", e)
                            uiState = uiState.copy(isHistoryLoading = false, isShiftInsightLoading = false)
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

    private suspend fun calculateShiftInsights(history: List<SleepResult>): ShiftInsightSummary {
        // Helper to get shift for a result's date
        fun getShiftTypeForResult(result: SleepResult): ShiftInsightType {
            val date = result.sleepEndTime.atZone(ZoneId.systemDefault()).toLocalDate()
            // We need a blocking or standard way to get shift here since we are in a non-suspend helper or 
            // the repository should provide a sync way if possible. 
            // For now, use runBlocking or ensure repository has cached values.
            val shiftCode = shiftScheduleRepository.getShiftForDate(date)
            return when (shiftCode) {
                "D" -> ShiftInsightType.DAY
                "E" -> ShiftInsightType.EVENING
                "N" -> ShiftInsightType.NIGHT
                else -> ShiftInsightType.OFF
            }
        }

        // Group results by shift
        val shiftMap = mutableMapOf<ShiftInsightType, MutableList<SleepResult>>()
        history.forEach { res ->
            val type = getShiftTypeForResult(res)
            shiftMap.getOrPut(type) { mutableListOf() }.add(res)
        }

        fun createInsight(type: ShiftInsightType): ShiftTypeInsight {
            val results = shiftMap[type] ?: emptyList()
            
            // For now, simplify and treat all as "Regular Pattern"
            // (Real transition logic can be added in next iteration)
            val regular = if (results.isNotEmpty()) {
                val sleepDurations = results.map { it.sleepDurationMinutes }

                val avgDur = sleepDurations.average().toLong()
                val (typicalMin, typicalMax) = calculateTypicalSleepDurationRange(sleepDurations)

                val avgFatigue = results.map { it.fatigueLevel.toDouble() }.average()
                val counts = results.groupingBy { it.morningGloryType.name }.eachCount()
                val mostCommon = counts.maxByOrNull { it.value }?.key

                ShiftPatternInsight(
                    sampleCount = results.size,
                    averageSleepDurationMinutes = avgDur,
                    averageFatigueLevel = avgFatigue,
                    mostCommonMorningGloryType = mostCommon,
                    morningGloryTypeCounts = counts,
                    featureText = if (results.size >= 3) "충분한 기록으로 분석된 패턴입니다." else "기록이 더 필요합니다.",
                    hasEnoughData = results.size >= 3,
                    typicalSleepDurationMinMinutes = typicalMin,
                    typicalSleepDurationMaxMinutes = typicalMax,
                )
            } else null

            return ShiftTypeInsight(
                shiftType = type,
                regularPattern = regular,
                transitionPattern = null // Placeholder for next step
            )
        }

        return ShiftInsightSummary(
            dayInsight = createInsight(ShiftInsightType.DAY),
            eveningInsight = createInsight(ShiftInsightType.EVENING),
            nightInsight = createInsight(ShiftInsightType.NIGHT),
            offInsight = createInsight(ShiftInsightType.OFF)
        )
    }

    private fun calculateTypicalSleepDurationRange(
        sleepDurations: List<Long>
    ): Pair<Long?, Long?> {
        if (sleepDurations.isEmpty()) return null to null

        val sorted = sleepDurations.sorted()

        if (sorted.size < 5) {
            return sorted.first() to sorted.last()
        }

        val lowerIndex = ((sorted.size - 1) * 0.05f).toInt()
        val upperIndex = ((sorted.size - 1) * 0.95f).toInt()

        return sorted[lowerIndex] to sorted[upperIndex]
    }
}
