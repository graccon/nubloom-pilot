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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationHelper
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray300
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.ui.theme.Gray200
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import java.time.Instant

@Composable
fun SleepPage() {
    val context = LocalContext.current
    val healthConnectRepository = remember { HealthConnectRepository(context) }
    val healthSummaryRepository = remember { HealthSummaryRepository(healthConnectRepository) }
    val shiftScheduleRepository = remember { ShiftScheduleRepository(context) }

    var latestSummary by remember { mutableStateOf<DailyHealthSummary?>(null) }
    var averageSleepDurationMinutes by remember { mutableStateOf<Long?>(null) }
    var averageShiftSleepDurationMinutes by remember { mutableStateOf<Long?>(null) }
    var todayShift by remember { mutableStateOf<String?>(null) }
    var averageWakeHeartRate by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
        isLoading = true
        if (healthConnectRepository.isHealthConnectAvailable() && healthConnectRepository.hasHealthPermissions()) {
            latestSummary = healthSummaryRepository.getLatestHealthSummary()
            
            // 1. Fetch general baseline (today + last 3 days)
            val recentSummaries = healthSummaryRepository.getRecentHealthSummaries(limit = 4)
            averageSleepDurationMinutes = healthSummaryRepository.getBaselineSleepDurationMinutes(recentSummaries)
            averageWakeHeartRate = healthSummaryRepository.getBaselineWakeHeartRate(recentSummaries)

            // 2. Fetch shift-specific baseline
            val today = LocalDate.now()
            todayShift = shiftScheduleRepository.getShiftForDate(today)

            if (!todayShift.isNullOrBlank()) {
                // Fetch more summaries to find 3 matches for the same shift
                val manyRecentSummaries = healthSummaryRepository.getRecentHealthSummaries(limit = 20)
                
                // Exclude today's summary from the baseline calculation
                val pastShiftSummaries = manyRecentSummaries.filter { summary ->
                    val summaryDate = summary.sleepEndTime.atZone(ZoneId.systemDefault()).toLocalDate()
                    val shiftOnThatDate = shiftScheduleRepository.getShiftForDate(summaryDate)
                    
                    // Same shift type AND not today
                    shiftOnThatDate == todayShift && summaryDate != today
                }

                if (pastShiftSummaries.size >= 3) {
                    averageShiftSleepDurationMinutes = pastShiftSummaries.take(3)
                        .map { it.sleepDurationMinutes }
                        .average()
                        .toLong()
                }
            }
        }
        isLoading = false
    }

    SleepPageContent(
        latestSummary = latestSummary,
        averageSleepDurationMinutes = averageSleepDurationMinutes,
        averageShiftSleepDurationMinutes = averageShiftSleepDurationMinutes,
        todayShift = todayShift,
        averageWakeHeartRate = averageWakeHeartRate,
        isLoading = isLoading,
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

private fun formatDuration(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}


@Composable
private fun SleepPageContent(
    latestSummary: DailyHealthSummary?,
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
        modifier = Modifier.padding(24.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SleepSummaryInfoCard(
                    modifier = Modifier.weight(1f),
                    firstLabel = "수면 시작 - 수면 종료",
                    firstValue = "$startTimeText - $endTimeText",
                    secondLabel = "깊은 수면",
                    secondValue = "${summary.deepSleepMinutes}분"
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

            ShiftSleepComparisonCard(
                todaySleepDurationMinutes = summary.sleepDurationMinutes,
                todayShift = todayShift,
                averageShiftSleepDurationMinutes = averageShiftSleepDurationMinutes
            )

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
    NubloomPilotTheme {
        SleepPageContent(
            latestSummary = DailyHealthSummary(
                sleepStartTime = Instant.now().minusSeconds(7 * 3600),
                sleepEndTime = Instant.now(),
                sleepDurationMinutes = 390L,
                deepSleepMinutes = 72L,
                wakeHeartRate = 68L,
                averageHrvMillis = null,
                stepsLast24Hours = 8420L
            ),
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

@Composable
private fun SleepDurationComparisonCard(
    todaySleepDurationMinutes: Long,
    averageSleepDurationMinutes: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Gray300
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SleepMetricRow(
                label = "오늘 총 수면시간",
                value = formatDuration(todaySleepDurationMinutes),
                labelStyle = MaterialTheme.typography.titleMedium,
                valueStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            SleepMetricRow(
                label = "최근 3일 평균",
                value = averageSleepDurationMinutes?.let {
                    formatDuration(it)
                } ?: "최근 수면 데이터가 부족해요",
                labelStyle = MaterialTheme.typography.bodyLarge,
                valueStyle = MaterialTheme.typography.bodyLarge
            )

            if (averageSleepDurationMinutes != null) {
                val diff = todaySleepDurationMinutes - averageSleepDurationMinutes
                val diffAbs = kotlin.math.abs(diff)

                val diffDescription = when {
                    diff > 0 -> "최근 3일 평균보다 ${formatDuration(diffAbs)} 더 주무셨어요."
                    diff < 0 -> "최근 3일 평균보다 ${formatDuration(diffAbs)} 적게 주무셨어요."
                    else -> "최근 3일 평균과 비슷하게 주무셨어요."
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = diffDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SleepMetricRow(
    label: String,
    value: String,
    labelStyle: androidx.compose.ui.text.TextStyle,
    valueStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = Gray800,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            style = valueStyle,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ShiftSleepComparisonCard(
    todaySleepDurationMinutes: Long,
    todayShift: String?,
    averageShiftSleepDurationMinutes: Long?
) {
    val shiftLabel = when(todayShift) {
        "D" -> "DAY"
        "E" -> "EVENING"
        "N" -> "NIGHT"
        "O" -> "휴일"
        else -> todayShift ?: ""
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Gray300
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            SleepMetricRow(
                label = "$shiftLabel 근무 최근 3회",
                value = "근무 기준 수면",
                labelStyle = MaterialTheme.typography.bodyLarge,
                valueStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (averageShiftSleepDurationMinutes != null) {
                SleepMetricRow(
                    label = "최근 3회 평균",
                    value = formatDuration(averageShiftSleepDurationMinutes),
                    labelStyle = MaterialTheme.typography.bodyLarge,
                    valueStyle = MaterialTheme.typography.bodyLarge
                )

                val diff = todaySleepDurationMinutes - averageShiftSleepDurationMinutes
                val diffAbs = kotlin.math.abs(diff)
                val diffDescription = when {
                    diff > 0 -> "오늘은 $shiftLabel 근무 평균보다 ${formatDuration(diffAbs)} 더 주무셨어요."
                    diff < 0 -> "오늘은 $shiftLabel 근무 평균보다 ${formatDuration(diffAbs)} 적게 주무셨어요."
                    else -> "오늘은 $shiftLabel 근무 평균과 비슷하게 주무셨어요."
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = diffDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "동일 근무 유형 수면 데이터가 부족해요. 3일의 데이터가 필요해요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray800
                )
            }
        }
    }
}

@Composable
private fun SleepSummaryInfoCard(
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Gray300
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = firstLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Gray800
            )

            Text(
                text = firstValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = secondLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Gray800
            )

            Text(
                text = secondValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

