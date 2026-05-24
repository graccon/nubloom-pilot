package com.sujin.nubloompilot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.HomeActionCard
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.components.SpiralTimeline
import com.sujin.nubloompilot.components.rememberCurrentTime
import com.sujin.nubloompilot.components.rememberRotatingMessage
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme

@Composable
fun HomePage(
    participantName: String,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    onNavigateToSleepCheckIn: (
        endTime: String,
        duration: Long,
        heartRate: Long?,
        baselineDuration: Long?,
        baselineHeartRate: Long?
    ) -> Unit,
    onNavigateToResult: (MorningGloryType) -> Unit,
    modifier: Modifier = Modifier,
    state: HomePageState = rememberHomePageState()
) {
    HomePageContent(
        participantName = participantName,
        yesterdayShift = yesterdayShift,
        todayShift = todayShift,
        tomorrowShift = tomorrowShift,
        dayAfterTomorrowShift = dayAfterTomorrowShift,
        uiState = state.uiState,
        onActionCardClick = {
            state.onActionCardClick(onNavigateToSleepCheckIn, onNavigateToResult)
        },
        modifier = modifier
    )
}

@Composable
private fun HomePageContent(
    participantName: String,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    uiState: HomePageUiState,
    onActionCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTime = rememberCurrentTime()
    val greetingMessage = rememberRotatingMessage()

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

        if (uiState.latestHealthSummary != null) {
            Spacer(modifier = Modifier.height(44.dp))

            ReportHeader(
                morningGloryType = uiState.morningGloryType
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeActionCard(
                type = uiState.morningGloryType,
                onClick = onActionCardClick
            )
        }

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
    morningGloryType: MorningGloryType?
) {
    val subtitle = if (morningGloryType == null) {
        "아직 오늘의 수면이 기록되지 않았어요."
    } else {
        "오늘의 나팔꽃이 피었어요!"
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

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    NubloomPilotTheme {
        HomePageContent(
            participantName = "수진",
            yesterdayShift = "N",
            todayShift = "N",
            tomorrowShift = "N",
            dayAfterTomorrowShift = "E",
            uiState = HomePageUiState(
                morningGloryType = null
            ),
            onActionCardClick = {}
        )
    }
}
