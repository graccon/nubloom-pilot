package com.sujin.nubloompilot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.SleepSessionRecord
import com.sujin.nubloompilot.components.HomeActionCard
import com.sujin.nubloompilot.components.SleepReportState
import com.sujin.nubloompilot.components.SpiralTimeline
import com.sujin.nubloompilot.components.rememberCurrentTime
import com.sujin.nubloompilot.components.rememberRotatingMessage
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.SleepStatusRepository
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun HomePage(
    participantName: String,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    onCheckInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val scope = rememberCoroutineScope()

    val currentTime = rememberCurrentTime()
    val greetingMessage = rememberRotatingMessage()

    val healthConnectRepository = remember {
        if (isPreview) null else HealthConnectRepository(context)
    }

    val sleepStatusRepository = remember {
        if (isPreview || healthConnectRepository == null) {
            null
        } else {
            SleepStatusRepository(
                healthConnectRepository = healthConnectRepository,
                sleepSurveyLocalStore = SleepSurveyLocalStore(context)
            )
        }
    }

    var sleepReportState by remember {
        mutableStateOf(SleepReportState.NONE)
    }

    var showSleepSheet by remember {
        mutableStateOf(false)
    }

    var recentSleepSessions by remember {
        mutableStateOf<List<SleepSessionRecord>>(emptyList())
    }

    LaunchedEffect(Unit) {
        if (sleepStatusRepository != null) {
            sleepReportState =
                sleepStatusRepository.getCurrentSleepReportState()
        }
    }

    if (showSleepSheet) {
        RecentSleepSessionSheet(
            sleepSessions = recentSleepSessions,
            onDismiss = {
                showSleepSheet = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 28.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeHeader(
            participantName = participantName,
            greetingMessage = greetingMessage
        )

        Spacer(modifier = Modifier.height(44.dp))

        SpiralTimeline(
            yesterdayShift = yesterdayShift,
            todayShift = todayShift,
            tomorrowShift = tomorrowShift,
            dayAfterTomorrowShift = dayAfterTomorrowShift,
            currentTime = currentTime
        )

        Spacer(modifier = Modifier.height(44.dp))

        ReportHeader(
            sleepReportState = sleepReportState
        )

        Spacer(modifier = Modifier.height(14.dp))

        HomeActionCard(
            state = sleepReportState,
            onClick = {
                if (healthConnectRepository == null) return@HomeActionCard

                scope.launch {
                    recentSleepSessions =
                        healthConnectRepository.getRecentSleepSessions(
                            limit = 3,
                            lookBackDays = 7
                        )

                    showSleepSheet = true
                }
            }
        )

        Spacer(modifier = Modifier.height(188.dp))
    }
}

@Composable
private fun HomeHeader(
    participantName: String,
    greetingMessage: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$participantName 선생님",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = greetingMessage,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray800
        )
    }
}

@Composable
private fun ReportHeader(
    sleepReportState: SleepReportState
) {
    val subtitle = when (sleepReportState) {
        SleepReportState.NONE -> "아직 오늘의 수면이 기록되지 않았어요."
        else -> "오늘의 나팔꽃이 피었어요!"
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = Gray300
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "오늘의 수면 리포트",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray800
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentSleepSessionSheet(
    sleepSessions: List<SleepSessionRecord>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "최근 수면 기록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (sleepSessions.isEmpty()) {
                Text(
                    text = "최근 수면 기록이 없습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray800
                )
            } else {
                sleepSessions.forEach { session ->
                    SleepSessionItem(
                        sleepSession = session
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SleepSessionItem(
    sleepSession: SleepSessionRecord
) {
    val zoneId = ZoneId.systemDefault()

    val startTime = sleepSession.startTime
        .atZone(zoneId)

    val endTime = sleepSession.endTime
        .atZone(zoneId)

    val duration = Duration.between(
        sleepSession.startTime,
        sleepSession.endTime
    )

    val dateFormatter = DateTimeFormatter.ofPattern("M월 d일")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = startTime.format(dateFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${startTime.format(timeFormatter)} ~ ${endTime.format(timeFormatter)} · $duration",
            style = MaterialTheme.typography.bodyLarge,
            color = Gray800
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    NubloomPilotTheme {
        HomePage(
            participantName = "수진",
            yesterdayShift = "N",
            todayShift = "N",
            tomorrowShift = "N",
            dayAfterTomorrowShift = "E",
            onCheckInClick = {}
        )
    }
}