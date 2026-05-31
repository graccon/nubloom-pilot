package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import androidx.annotation.DrawableRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import com.sujin.nubloompilot.R

@Composable
fun CheckInGrassGrid(
    checkInHistory: List<SleepResult>,
    modifier: Modifier = Modifier,
    weeksCount: Int = 12
) {
    val today = LocalDate.now()
    val checkInMap = checkInHistory
        .sortedBy { it.timestamp }
        .associateBy { result ->
            result.sleepEndTime.atZone(ZoneId.systemDefault()).toLocalDate()
        }

    val currentSunday = today.with(
        TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)

    )

    val firstSunday = currentSunday.minusWeeks((weeksCount - 1).toLong())

    val scrollState = rememberScrollState()

    // Scroll to the end (latest date) on initial load
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "최근 수면 체크인 기록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        
        Spacer(modifier = Modifier.height(10.dp))

        // Grid showing the "grass" like GitHub contribution graph
        // Rows: Days of week (Mon-Sun), Columns: Weeks
        val itemSize = 36.dp
        val cellGap = 4.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(cellGap)) {
                // Day of week rows: 0=Monday, 6=Sunday
                (0 until 7).forEach { dayIndex ->
                    Row(horizontalArrangement = Arrangement.spacedBy(cellGap)) {
                        (0 until weeksCount).forEach { weekIndex ->
                            // Calculate the date for this specific cell
                            val date = firstSunday
                                .plusWeeks(weekIndex.toLong())
                                .plusDays(dayIndex.toLong())

                            // Only show if the date is not in the future
                            val isFuture = date.isAfter(today)

                            GrassCell(
                                modifier = Modifier.size(itemSize),
                                type = if (isFuture) null else checkInMap[date]?.morningGloryType,
                                isPlaceholder = isFuture
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrassCell(
    modifier: Modifier = Modifier,
    type: MorningGloryType?,
    isPlaceholder: Boolean = false
) {
    val icon = when (type) {
        MorningGloryType.TYPE_1 -> "🌸"
        MorningGloryType.TYPE_2 -> "🌱"
        MorningGloryType.TYPE_3 -> "🌙"
        MorningGloryType.TYPE_4 -> "💤"
        null -> ""
    }

    val backgroundColor = when {
        isPlaceholder -> Color.Transparent
        type == null -> Gray300
        else -> Color.Transparent
    }
    val iconRes = type.toIconRes()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckInGrassGridPreview() {
    val today = LocalDate.now()
    val dummyHistory = listOf(
        SleepResult(
            participantId = "test",
            participantName = "test",
            sleepEndTime = today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            sleepDurationMinutes = 480,
            wakeHeartRate = 70,
            fatigueLevel = 3,
            morningGloryType = MorningGloryType.TYPE_1,
            timestamp = System.currentTimeMillis()
        ),
        SleepResult(
            participantId = "test",
            participantName = "test",
            sleepEndTime = today.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            sleepDurationMinutes = 400,
            wakeHeartRate = 72,
            fatigueLevel = 4,
            morningGloryType = MorningGloryType.TYPE_3,
            timestamp = System.currentTimeMillis() - 2 * 24 * 3600 * 1000
        ),
        SleepResult(
            participantId = "test",
            participantName = "test",
            sleepEndTime = today.minusDays(8).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            sleepDurationMinutes = 350,
            wakeHeartRate = 75,
            fatigueLevel = 2,
            morningGloryType = MorningGloryType.TYPE_2,
            timestamp = System.currentTimeMillis() - 8 * 24 * 3600 * 1000
        )
    )

    NubloomPilotTheme {
        CheckInGrassGrid(checkInHistory = dummyHistory)
    }
}


@DrawableRes

private fun MorningGloryType?.toIconRes(): Int? {

    return when (this) {
        MorningGloryType.TYPE_1 -> R.drawable.grass_grid_1
        MorningGloryType.TYPE_2 -> R.drawable.grass_grid_2
        MorningGloryType.TYPE_3 -> R.drawable.grass_grid_3
        MorningGloryType.TYPE_4 -> R.drawable.grass_grid_4
        null -> null
    }

}