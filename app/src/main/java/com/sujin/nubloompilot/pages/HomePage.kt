package com.sujin.nubloompilot.pages

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.HomeActionCard
import com.sujin.nubloompilot.components.SpiralTimeline
import com.sujin.nubloompilot.components.TopBanner
import com.sujin.nubloompilot.components.rememberCurrentTime
import com.sujin.nubloompilot.components.rememberRotatingMessage
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SavedSleepInterventionBundle
import com.sujin.nubloompilot.models.SavedSleepIntervention
import com.sujin.nubloompilot.models.TimelineMarker
import com.sujin.nubloompilot.ui.theme.*
import com.sujin.nubloompilot.utils.InterventionToMarkerMapper
import com.sujin.nubloompilot.utils.hasActiveIntervention
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Button
import com.sujin.nubloompilot.components.TopBannerManager
import java.time.Instant

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
    state: HomePageState = rememberHomePageState(),
    topBannerManager: TopBannerManager
) {
    Box(modifier = modifier.fillMaxSize()) {
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
            onDebugSleepCheckInClick = {
                onNavigateToSleepCheckIn(
                    Instant.now().toString(),
                    420L,
                    68L,
                    450L,
                    70L
                )
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(state.uiState.showForegroundBanner) {
            if (state.uiState.showForegroundBanner) {
                topBannerManager.show("최신 수면 데이터를 확인했어요")
            }
        }
    }
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
    onDebugSleepCheckInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTime = rememberCurrentTime()
    val greetingMessage = rememberRotatingMessage()

    val timelineRevealProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        timelineRevealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            )
        )
    }


    val isInterventionValid = remember(latestInterventionBundle, currentTime) {
        latestInterventionBundle.hasActiveIntervention()
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

    var expandedInterventionId by remember { mutableStateOf<String?>(null) }

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

//        Button(
//            onClick = onDebugSleepCheckInClick,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("테스트 수면 체크인")
//        }

        SpiralTimeline(
            yesterdayShift = yesterdayShift,
            todayShift = todayShift,
            tomorrowShift = tomorrowShift,
            dayAfterTomorrowShift = dayAfterTomorrowShift,
            currentTime = currentTime,
            markers = timelineMarkers,
            highlightedMarkerId = expandedInterventionId,
            revealProgress = timelineRevealProgress.value
        )
        Spacer(modifier = Modifier.height(30.dp))

        if (activeInterventions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            InterventionSection(
                interventions = activeInterventions,
                expandedId = expandedInterventionId,
                onExpandedIdChange = { id ->
                    expandedInterventionId = if (expandedInterventionId == id) null else id
                }
            )
        }

        if (uiState.latestHealthSummary != null) {
            Spacer(modifier = Modifier.height(20.dp))

            ReportHeader(
                morningGloryType = uiState.morningGloryType
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeActionCard(
                type = uiState.morningGloryType,
                onClick = onActionCardClick
            )
        }

        Spacer(modifier = Modifier.height(88.dp))
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
    interventions: List<SavedSleepIntervention>,
    expandedId: String?,
    onExpandedIdChange: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        interventions.forEach { intervention ->
            InterventionItem(
                intervention = intervention,
                isExpanded = intervention.startTime == expandedId,
                onToggleExpand = { onExpandedIdChange(intervention.startTime) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InterventionItem(
    intervention: SavedSleepIntervention,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
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

    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8E8E8))
            .clickable { onToggleExpand() }
            .animateContentSize()
            .padding(10.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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

                if (formattedStart.isNotEmpty() && formattedEnd.isNotEmpty()) {
                    Text(
                        text = "$formattedStart ~ $formattedEnd",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Gray600,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationState)
            )
        }

        if (isExpanded) {
            Text(
                text = intervention.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

