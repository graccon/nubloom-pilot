package com.sujin.nubloompilot.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.components.HomeActionCard
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.components.SpiralTimeline
import com.sujin.nubloompilot.components.rememberCurrentTime
import com.sujin.nubloompilot.components.rememberRotatingMessage
import com.sujin.nubloompilot.models.SavedSleepInterventionBundle
import com.sujin.nubloompilot.models.SavedSleepIntervention
import com.sujin.nubloompilot.models.TimelineMarker
import com.sujin.nubloompilot.ui.theme.*
import com.sujin.nubloompilot.utils.InterventionToMarkerMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomePage(
    participantName: String,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    latestInterventionBundle: SavedSleepInterventionBundle?,
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
        latestInterventionBundle = latestInterventionBundle,
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
    latestInterventionBundle: SavedSleepInterventionBundle?,
    uiState: HomePageUiState,
    onActionCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTime = rememberCurrentTime()
    val greetingMessage = rememberRotatingMessage()

    val isInterventionValid = remember(latestInterventionBundle) {
        latestInterventionBundle != null && 
        latestInterventionBundle.workDate == LocalDate.now().toString()
    }

    val timelineMarkers = remember(latestInterventionBundle, isInterventionValid) {
        if (isInterventionValid && latestInterventionBundle != null) {
            InterventionToMarkerMapper.map(
                interventions = latestInterventionBundle.interventions,
                referenceDate = LocalDate.now()
            )
        } else {
            emptyList()
        }
    }

    val activeInterventions = remember(latestInterventionBundle, isInterventionValid, currentTime) {
        if (isInterventionValid && latestInterventionBundle != null) {
            val now = LocalDateTime.now()
            latestInterventionBundle.interventions
                .filter { intervention ->
                    val end = runCatching { LocalDateTime.parse(intervention.endTime) }.getOrNull()
                    end?.isAfter(now) ?: false
                }
                .sortedBy { it.startTime }
                .take(2)
        } else {
            emptyList()
        }
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
            currentTime = currentTime,
            markers = timelineMarkers
        )

        if (activeInterventions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(44.dp))
            InterventionSection(interventions = activeInterventions)
        }

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

@Composable
private fun InterventionSection(
    interventions: List<SavedSleepIntervention>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        interventions.forEach { intervention ->
            InterventionItem(intervention = intervention)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InterventionItem(
    intervention: SavedSleepIntervention
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val formattedStart = remember(intervention.startTime) {
        runCatching {
            LocalDateTime.parse(intervention.startTime).format(timeFormatter)
        }.getOrDefault("")
    }

    val formattedEnd = remember(intervention.endTime) {
        runCatching {
            LocalDateTime.parse(intervention.endTime).format(timeFormatter)
        }.getOrDefault("")
    }

    val iconRes = remember(intervention) {
        InterventionToMarkerMapper.getIconRes(
            intervention.type,
            intervention.actionType
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFE8E8E8),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = intervention.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(2.dp))

                if (formattedStart.isNotEmpty() && formattedEnd.isNotEmpty()) {
                    Text(
                        text = "$formattedStart ~ $formattedEnd",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = intervention.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800,
            modifier = Modifier.padding(horizontal = 4.dp)
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
            latestInterventionBundle = null,
            uiState = HomePageUiState(
                morningGloryType = null
            ),
            onActionCardClick = {}
        )
    }
}
