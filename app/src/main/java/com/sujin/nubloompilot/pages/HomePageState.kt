package com.sujin.nubloompilot.pages

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.sujin.nubloompilot.components.SleepReportState
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.SleepStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class HomePageUiState(
    val latestHealthSummary: DailyHealthSummary? = null,
    val sleepReportState: SleepReportState = SleepReportState.NONE,
    val baselineSleepDurationMinutes: Long? = null,
    val baselineWakeHeartRate: Long? = null
)

@Stable
class HomePageState(
    private val healthSummaryRepository: HealthSummaryRepository,
    private val sleepStatusRepository: SleepStatusRepository,
    private val scope: CoroutineScope
) {
    var uiState by mutableStateOf(HomePageUiState())
        private set

    fun loadData() {
        scope.launch {
            val summary = healthSummaryRepository.getLatestHealthSummary()
            val reportState = if (summary == null) {
                SleepReportState.NONE
            } else {
                sleepStatusRepository.getCurrentSleepReportState()
            }

            uiState = uiState.copy(
                latestHealthSummary = summary,
                sleepReportState = reportState
            )
        }
    }

    fun onActionCardClick(
        onNavigateToSleepCheckIn: (
            duration: Long,
            heartRate: Long?,
            baselineDuration: Long?,
            baselineHeartRate: Long?
        ) -> Unit
    ) {
        val summary = uiState.latestHealthSummary ?: return

        onNavigateToSleepCheckIn(
            summary.sleepDurationMinutes,
            summary.wakeHeartRate,
            uiState.baselineSleepDurationMinutes,
            uiState.baselineWakeHeartRate
        )
    }
}

@Composable
fun rememberHomePageState(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
): HomePageState {
    val healthConnectRepository = remember { HealthConnectRepository(context) }
    val sleepStatusRepository = remember {
        SleepStatusRepository(
            healthConnectRepository = healthConnectRepository,
            sleepSurveyLocalStore = SleepSurveyLocalStore(context)
        )
    }
    val healthSummaryRepository = remember { HealthSummaryRepository(healthConnectRepository) }

    val state = remember {
        HomePageState(
            healthSummaryRepository = healthSummaryRepository,
            sleepStatusRepository = sleepStatusRepository,
            scope = scope
        )
    }

    LaunchedEffect(Unit) {
        state.loadData()
    }

    return state
}
