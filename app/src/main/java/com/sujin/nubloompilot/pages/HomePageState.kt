package com.sujin.nubloompilot.pages

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.SleepStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

data class HomePageUiState(
    val latestHealthSummary: DailyHealthSummary? = null,
    val morningGloryType: MorningGloryType? = null,
    val baselineSleepDurationMinutes: Long? = null,
    val baselineWakeHeartRate: Long? = null,
    val showForegroundBanner: Boolean = false
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

    fun showForegroundRefreshMessage() {
        scope.launch {
            uiState = uiState.copy(showForegroundBanner = true)
            delay(2000)
            uiState = uiState.copy(showForegroundBanner = false)
        }
    }

    fun onDebugSleepCheckInClick(
        onNavigateToSleepCheckIn: (
            endTime: String,
            duration: Long,
            heartRate: Long?,
            baselineDuration: Long?,
            baselineHeartRate: Long?
        ) -> Unit
    ) {
        onNavigateToSleepCheckIn(
            Instant.now().toString(),
            420L,
            68L,
            450L,
            70L
        )
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state.loadData()
                state.showForegroundRefreshMessage()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        state.loadData()
    }

    return state
}


