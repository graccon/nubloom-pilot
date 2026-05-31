package com.sujin.nubloompilot.pages

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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class SleepPageUiState(
    val latestSummary: DailyHealthSummary? = null,
    val recentSummaries: List<DailyHealthSummary> = emptyList(),
    val checkInHistory: List<SleepResult> = emptyList(),
    val averageSleepDurationMinutes: Long? = null,
    val averageShiftSleepDurationMinutes: Long? = null,
    val todayShift: String? = null,
    val averageWakeHeartRate: Long? = null,
    val isLoading: Boolean = false,
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
        // 이미 성공적으로 로딩했다면 재조회 방지
        if (hasLoadedOnce && uiState.latestSummary != null) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            
            try {
                if (healthConnectRepository.isHealthConnectAvailable() && healthConnectRepository.hasHealthPermissions()) {
                    val latestSummary = healthSummaryRepository.getLatestHealthSummary()
                    
                    // 1. 일반 베이스라인 가져오기 (오늘 + 지난 3일)
                    val recentSummaries = healthSummaryRepository.getRecentHealthSummaries(limit = 4)
                    val averageSleepDurationMinutes = healthSummaryRepository.getBaselineSleepDurationMinutes(recentSummaries)
                    val averageWakeHeartRate = healthSummaryRepository.getBaselineWakeHeartRate(recentSummaries)

                    // 2. 수면 체크인 역사 가져오기 (잔디 UI용)
                    val checkInHistory = sleepResultRepository.getSleepResultsInDateRange(days = 30)

                    // 3. 근무조별 베이스라인 가져오기
                    val today = LocalDate.now()
                    val todayShift = shiftScheduleRepository.getShiftForDate(today)
                    var averageShiftSleepDurationMinutes: Long? = null

                    if (!todayShift.isNullOrBlank()) {
                        val manyRecentSummaries = healthSummaryRepository.getRecentHealthSummaries(limit = 20)
                        val pastShiftSummaries = manyRecentSummaries.filter { summary ->
                            val summaryDate = summary.sleepEndTime.atZone(ZoneId.systemDefault()).toLocalDate()
                            val shiftOnThatDate = shiftScheduleRepository.getShiftForDate(summaryDate)
                            shiftOnThatDate == todayShift && summaryDate != today
                        }

                        if (pastShiftSummaries.size >= 3) {
                            averageShiftSleepDurationMinutes = pastShiftSummaries.take(3)
                                .map { it.sleepDurationMinutes }
                                .average()
                                .toLong()
                        }
                    }

                    uiState = uiState.copy(
                        latestSummary = latestSummary,
                        recentSummaries = recentSummaries,
                        checkInHistory = checkInHistory,
                        averageSleepDurationMinutes = averageSleepDurationMinutes,
                        averageShiftSleepDurationMinutes = averageShiftSleepDurationMinutes,
                        todayShift = todayShift,
                        averageWakeHeartRate = averageWakeHeartRate
                    )
                    hasLoadedOnce = true
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "데이터를 불러오는 중 오류가 발생했습니다.")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}
