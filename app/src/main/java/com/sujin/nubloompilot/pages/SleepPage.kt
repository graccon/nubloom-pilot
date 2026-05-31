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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationHelper
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.pages.sleep.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import java.time.Instant

@Composable
fun SleepPage() {
    val context = LocalContext.current
    val healthConnectRepository = remember { HealthConnectRepository(context) }
    val healthSummaryRepository = remember { HealthSummaryRepository(healthConnectRepository) }
    val shiftScheduleRepository = remember { ShiftScheduleRepository(context) }

    val viewModel: SleepViewModel = viewModel(
        factory = SleepViewModel.Factory(
            healthSummaryRepository = healthSummaryRepository,
            healthConnectRepository = healthConnectRepository,
            shiftScheduleRepository = shiftScheduleRepository
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
        averageSleepDurationMinutes = uiState.averageSleepDurationMinutes,
        averageShiftSleepDurationMinutes = uiState.averageShiftSleepDurationMinutes,
        todayShift = uiState.todayShift,
        averageWakeHeartRate = uiState.averageWakeHeartRate,
        isLoading = uiState.isLoading,
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
    averageSleepDurationMinutes: Long?,
    averageShiftSleepDurationMinutes: Long?,
    todayShift: String?,
    averageWakeHeartRate: Long?,
    isLoading: Boolean,
    permissionStatus: String,
    notificationRequestStatus: String,
    onRequestNotificationPermission: () -> Unit,
    onShowSleepCheckInNotification: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "오늘의 수면 데이터",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Text("데이터를 불러오는 중...")
        } else if (latestSummary != null) {
            val summary = latestSummary
            val startTimeText = summary.sleepStartTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            val endTimeText = summary.sleepEndTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            

            SleepDurationComparisonCard(
                todaySleepDurationMinutes = summary.sleepDurationMinutes,
                averageSleepDurationMinutes = averageSleepDurationMinutes
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
                averageShiftSleepDurationMinutes = averageShiftSleepDurationMinutes
            )

            Spacer(modifier = Modifier.height(24.dp))

            SleepTimelineBarChart(recentSummaries = recentSummaries)

            Spacer(modifier = Modifier.height(16.dp))

        } else {
            Text("감지된 수면 데이터가 없습니다.")
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "알림 권한 테스트",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = permissionStatus)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestNotificationPermission
        ) {
            Text("알림 권한 요청")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isNotificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionStatus == "알림 권한 허용됨"
        } else {
            true
        }

        Button(
            onClick = onShowSleepCheckInNotification,
            enabled = isNotificationEnabled
        ) {
            Text("수면 체크인 알림 테스트")
        }

        if (notificationRequestStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notificationRequestStatus,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SleepPagePreview() {
    val now = Instant.now()
    val yesterday = now.minusSeconds(24 * 3600)
    
    val dummySummaries = listOf(
        DailyHealthSummary(
            sleepStartTime = now.minusSeconds(8 * 3600),
            sleepEndTime = now.minusSeconds(1 * 3600),
            sleepDurationMinutes = 420L,
            lightSleepMinutes = 240L,
            deepSleepMinutes = 72L,
            remSleepMinutes = 80L,
            awakeSleepMinutes = 20L,
            wakeHeartRate = 65L,
            averageHrvMillis = null,
            stepsLast24Hours = 7000L
        ),
        DailyHealthSummary(
            sleepStartTime = yesterday.minusSeconds(7 * 3600),
            sleepEndTime = yesterday.plusSeconds(1 * 3600),
            sleepDurationMinutes = 480L,
            lightSleepMinutes = 300L,
            deepSleepMinutes = 60L,
            remSleepMinutes = 90L,
            awakeSleepMinutes = 30L,
            wakeHeartRate = 68L,
            averageHrvMillis = null,
            stepsLast24Hours = 8000L
        )
    )

    NubloomPilotTheme {
        SleepPageContent(
            latestSummary = dummySummaries[0],
            recentSummaries = dummySummaries,
            averageSleepDurationMinutes = 420L,
            averageShiftSleepDurationMinutes = 360L,
            todayShift = "E",
            averageWakeHeartRate = 72L,
            isLoading = false,
            permissionStatus = "알림 권한 허용됨",
            notificationRequestStatus = "",
            onRequestNotificationPermission = {},
            onShowSleepCheckInNotification = {}
        )
    }
}


