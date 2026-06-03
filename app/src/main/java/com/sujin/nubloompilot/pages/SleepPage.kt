package com.sujin.nubloompilot.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationHelper
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.repository.SleepResultRepository
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.pages.sleep.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import java.time.Instant

@Composable
fun SleepPage() {
    val context = LocalContext.current
    val healthConnectRepository = remember { HealthConnectRepository(context) }
    val healthSummaryRepository = remember { HealthSummaryRepository(healthConnectRepository) }
    val shiftScheduleRepository = remember { ShiftScheduleRepository(context) }
    
    val participantId = remember { 
        ParticipantLocalStore(context).getParticipantId() ?: "unknown" 
    }
    val sleepResultRepository = remember(participantId) {
        SleepResultRepository(participantId, SleepSurveyLocalStore(context))
    }

    val viewModel: SleepViewModel = viewModel(
        factory = SleepViewModel.Factory(
            healthSummaryRepository = healthSummaryRepository,
            healthConnectRepository = healthConnectRepository,
            shiftScheduleRepository = shiftScheduleRepository,
            sleepResultRepository = sleepResultRepository
        )
    )

    val uiState = viewModel.uiState
// ... (omitting middle parts for brevity, but I will include them in the actual write)


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
    var selectedSleepSummary by remember(sleepSummariesLast24h) {
        mutableStateOf(sleepSummariesLast24h.maxByOrNull { it.sleepDurationMinutes })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "오늘의 수면 데이터",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        // TODO : 설명글 - 영역을 눌러 세부 잠의 정보를 살펴보세요.
        
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Text("데이터를 불러오는 중...")
        } else if (latestSummary != null) {
            val total24hSleepMinutes = if (sleepSummariesLast24h.isNotEmpty()) {
                sleepSummariesLast24h.sumOf { it.sleepDurationMinutes }
            } else {
                latestSummary.sleepDurationMinutes
            }

            val displayedSummary = selectedSleepSummary ?: latestSummary

            // Display 24h summaries timeline if available
            if (sleepSummariesLast24h.isNotEmpty()) {
                SleepLast24hTimeline(
                    summaries = sleepSummariesLast24h,
                    selectedSummary = selectedSleepSummary,
                    onSummarySelected = { selectedSleepSummary = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            SelectedSleepSummarySection(
                summary = displayedSummary,
                total24hSleepMinutes = total24hSleepMinutes,
                hasMultipleSleepSummaries = sleepSummariesLast24h.size > 1,
                averageSleepDurationMinutes = averageSleepDurationMinutes,
                averageShiftSleepDurationMinutes = averageShiftSleepDurationMinutes,
                todayShift = todayShift,
                averageWakeHeartRate = averageWakeHeartRate,
                isBaselineLoading = isBaselineLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            SleepTimelineBarChart(recentSummaries = recentSummaries)

            Spacer(modifier = Modifier.height(24.dp))

            if (isHistoryLoading) {
                Text("체크인 기록 불러오는 중...", color = Gray500)
            } else {
                CheckInGrassGrid(checkInHistory = checkInHistory)
            }

            Spacer(modifier = Modifier.height(24.dp))

        } else {
            Text("감지된 수면 데이터가 없습니다.")
        }

        Spacer(modifier = Modifier.height(44.dp))

//        NotificationTestSection(
//            permissionStatus = permissionStatus,
//            notificationRequestStatus = notificationRequestStatus,
//            onRequestPermission = onRequestNotificationPermission,
//            onTestNotification = onShowSleepCheckInNotification
//        )
    }
}

@Composable
private fun NotificationTestSection(
    permissionStatus: String,
    notificationRequestStatus: String,
    onRequestPermission: () -> Unit,
    onTestNotification: () -> Unit
) {
    Column {
        Text(
            text = "알림 권한 테스트",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text(
            text = permissionStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("알림 권한 요청")
        }

        Spacer(modifier = Modifier.height(8.dp))

        val isNotificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionStatus == "알림 권한 허용됨"
        } else {
            true
        }

        Button(
            onClick = onTestNotification,
            enabled = isNotificationEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("수면 체크인 알림 테스트")
        }

        if (notificationRequestStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notificationRequestStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SelectedSleepSummarySection(
    summary: DailyHealthSummary,
    total24hSleepMinutes: Long,
    hasMultipleSleepSummaries: Boolean,
    averageSleepDurationMinutes: Long?,
    averageShiftSleepDurationMinutes: Long?,
    todayShift: String?,
    averageWakeHeartRate: Long?,
    isBaselineLoading: Boolean
) {
    val startTimeText = summary.sleepStartTime.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    val endTimeText = summary.sleepEndTime.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    Column {
        SleepDurationComparisonCard(
            title = if (hasMultipleSleepSummaries) "선택된 수면의 시간" else "오늘 총 수면시간",
            todayAllSleepDurationMinutes = total24hSleepMinutes,
            todaySleepDurationMinutes = summary.sleepDurationMinutes,
            averageSleepDurationMinutes = if (isBaselineLoading) null else averageSleepDurationMinutes
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SleepSummaryInfoCard(
                modifier = Modifier.weight(1f),
                firstLabel = "수면 시작 - 수면 종료",
                firstValue = "$startTimeText - $endTimeText",
                secondLabel = "깬 시간",
                secondValue = "${summary.awakeSleepMinutes}분"
            )
            SleepSummaryInfoCard(
                modifier = Modifier.weight(1f),
                firstLabel = "기상 심박수",
                firstValue = "${summary.wakeHeartRate ?: "--"} bpm",
                secondLabel = "평균 심박수",
                secondValue = "${averageWakeHeartRate ?: "--"} bpm"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Gray300
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SleepStageStackedBar(
                    lightSleepMinutes = summary.lightSleepMinutes,
                    deepSleepMinutes = summary.deepSleepMinutes,
                    remSleepMinutes = summary.remSleepMinutes,
                    awakeSleepMinutes = summary.awakeSleepMinutes
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ShiftSleepComparisonCard(
            todaySleepDurationMinutes = summary.sleepDurationMinutes,
            todayShift = todayShift,
            averageShiftSleepDurationMinutes = if (isBaselineLoading) null else averageShiftSleepDurationMinutes
        )
    }
}
