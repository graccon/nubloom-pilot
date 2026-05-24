package com.sujin.nubloompilot.pages

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.SleepStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class HomePageUiState(
    val latestHealthSummary: DailyHealthSummary? = null,
    val morningGloryType: MorningGloryType? = null,
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
            val morningGloryType = if (summary == null) {
                null
            } else {
                sleepStatusRepository.getCurrentMorningGloryType()
            }

            uiState = uiState.copy(
                latestHealthSummary = summary,
                morningGloryType = morningGloryType
            )
        }
    }

    fun onActionCardClick(
        onNavigateToSleepCheckIn: (
            endTime: String,
            duration: Long,
            heartRate: Long?,
            baselineDuration: Long?,
            baselineHeartRate: Long?
        ) -> Unit,
        onNavigateToResult: (MorningGloryType) -> Unit
    ) {
        val summary = uiState.latestHealthSummary ?: return
        val currentType = uiState.morningGloryType

        if (currentType == null) {
            onNavigateToSleepCheckIn(
                summary.sleepEndTime.toString(),
                summary.sleepDurationMinutes,
                summary.wakeHeartRate,
                uiState.baselineSleepDurationMinutes,
                uiState.baselineWakeHeartRate
            )
        } else {
            onNavigateToResult(currentType)
        }
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
