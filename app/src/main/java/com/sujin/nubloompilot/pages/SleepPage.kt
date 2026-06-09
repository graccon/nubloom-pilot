package com.sujin.nubloompilot.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.RecoveryRhythmGraphData
import com.sujin.nubloompilot.models.RecoveryRhythmMarker
import com.sujin.nubloompilot.models.RecoveryRhythmMarkerType
import com.sujin.nubloompilot.models.RecoveryRhythmSeries
import com.sujin.nubloompilot.models.RecoveryRhythmSeriesPoint
import com.sujin.nubloompilot.models.ShiftInsightSummary
import com.sujin.nubloompilot.models.ShiftInsightType
import com.sujin.nubloompilot.models.ShiftPatternInsight
import com.sujin.nubloompilot.models.ShiftTypeInsight
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationHelper
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.repository.SleepResultRepository
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.pages.sleep.*

@Composable
fun SleepPage() {
    val context = LocalContext.current
    val healthConnectRepository = remember { HealthConnectRepository(context) }
    val healthSummaryRepository = remember { HealthSummaryRepository(healthConnectRepository) }
    val shiftScheduleRepository = remember { ShiftScheduleRepository(context) }
    
    val participantLocalStore = remember { ParticipantLocalStore(context) }
    val participantId = remember { 
        participantLocalStore.getParticipantId() ?: "unknown" 
    }
    val sleepResultRepository = remember(participantId) {
        SleepResultRepository(participantId, SleepSurveyLocalStore(context))
    }

    val viewModel: SleepViewModel = viewModel(
        factory = SleepViewModel.Factory(
            healthSummaryRepository = healthSummaryRepository,
            healthConnectRepository = healthConnectRepository,
            shiftScheduleRepository = shiftScheduleRepository,
            sleepResultRepository = sleepResultRepository,
            participantLocalStore = participantLocalStore
        )
    )

    val uiState = viewModel.uiState

    var permissionStatus by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                "이 버전에서는 알림 권한 요청이 필요하지 않습니다"
            } else {
                "알림 권한 미확인"
            }
        )
    }
    var notificationRequestStatus by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = if (isGranted) "알림 권한 허용됨" else "알림 권한 거부됨"
    }

    LaunchedEffect(Unit) {
        viewModel.loadSleepData()
    }

    SleepPageContent(
        latestSummary = uiState.latestSummary,
        recentSummaries = uiState.recentSummaries,
        sleepSummariesLast24h = uiState.sleepSummariesLast24h,
        checkInHistory = uiState.checkInHistory,
        shiftInsightSummary = uiState.shiftInsightSummary,
        averageSleepDurationMinutes = uiState.averageSleepDurationMinutes,
        averageShiftSleepDurationMinutes = uiState.averageShiftSleepDurationMinutes,
        todayShift = uiState.todayShift,
        averageWakeHeartRate = uiState.averageWakeHeartRate,
        isLoading = uiState.isLoading,
        isHistoryLoading = uiState.isHistoryLoading,
        isBaselineLoading = uiState.isBaselineLoading,
        permissionStatus = permissionStatus,
        notificationRequestStatus = notificationRequestStatus,
        onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onShowSleepCheckInNotification = {
            val hasNotificationPermission =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

            if (hasNotificationPermission) {
                SleepCheckInNotificationHelper.showSleepCheckInNotification(context)
                notificationRequestStatus = "수면 체크인 알림을 요청했습니다"
            } else {
                notificationRequestStatus = "알림 권한이 없어 알림을 표시할 수 없습니다"
            }
        }
    )
}


@Composable
private fun SleepPageContent(
    latestSummary: DailyHealthSummary?,
    recentSummaries: List<DailyHealthSummary>,
    sleepSummariesLast24h: List<DailyHealthSummary>,
    checkInHistory: List<com.sujin.nubloompilot.models.SleepResult>,
    shiftInsightSummary: ShiftInsightSummary?,
    averageSleepDurationMinutes: Long?,
    averageShiftSleepDurationMinutes: Long?,
    todayShift: String?,
    averageWakeHeartRate: Long?,
    isLoading: Boolean,
    isHistoryLoading: Boolean,
    isBaselineLoading: Boolean,
    permissionStatus: String,
    notificationRequestStatus: String,
    onRequestNotificationPermission: () -> Unit,
    onShowSleepCheckInNotification: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SleepPageTab.RECENT_RECOVERY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        
        Spacer(modifier = Modifier.height(12.dp))

        SleepPageTabToggle(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            SleepPageTab.RECENT_RECOVERY -> {
                RecentRecoverySection(
                    latestSummary = latestSummary,
                    recentSummaries = recentSummaries,
                    sleepSummariesLast24h = sleepSummariesLast24h,
                    checkInHistory = checkInHistory,
                    averageSleepDurationMinutes = averageSleepDurationMinutes,
                    averageShiftSleepDurationMinutes = averageShiftSleepDurationMinutes,
                    todayShift = todayShift,
                    averageWakeHeartRate = averageWakeHeartRate,
                    isLoading = isLoading,
                    isHistoryLoading = isHistoryLoading,
                    isBaselineLoading = isBaselineLoading
                )
            }
            SleepPageTab.SHIFT_INSIGHT -> {
                ShiftInsightPlaceholderSection(shiftInsightSummary = shiftInsightSummary)
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }
}
