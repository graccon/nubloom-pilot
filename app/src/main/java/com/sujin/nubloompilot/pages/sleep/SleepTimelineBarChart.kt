package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray800
import java.time.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun SleepTimelineBarChart(
    recentSummaries: List<DailyHealthSummary>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val datesToShow = listOf(today, today.minusDays(1), today.minusDays(2))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "최근 3일 수면 타임라인",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )

        datesToShow.forEach { date ->
            SleepTimelineRow(
                date = date,
                allSummaries = recentSummaries
            )
        }

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("00:00", style = MaterialTheme.typography.labelSmall, color = Gray800)
            Text("06:00", style = MaterialTheme.typography.labelSmall, color = Gray800)
            Text("12:00", style = MaterialTheme.typography.labelSmall, color = Gray800)
            Text("18:00", style = MaterialTheme.typography.labelSmall, color = Gray800)
            Text("24:00", style = MaterialTheme.typography.labelSmall, color = Gray800)
        }
    }
}

@Composable
private fun SleepTimelineRow(
    date: LocalDate,
    allSummaries: List<DailyHealthSummary>
) {
    val zoneId = ZoneId.systemDefault()
    val dayStart = date.atStartOfDay(zoneId).toInstant()
    val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()

    // Find all sleep segments that overlap with this calendar day
    val segments = allSummaries.mapNotNull { s ->
        val overlapStart = if (s.sleepStartTime.isAfter(dayStart)) s.sleepStartTime else dayStart
        val overlapEnd = if (s.sleepEndTime.isBefore(dayEnd)) s.sleepEndTime else dayEnd

        if (overlapStart.isBefore(overlapEnd)) {
            val startMinutes = ChronoUnit.MINUTES.between(dayStart, overlapStart)
            val endMinutes = ChronoUnit.MINUTES.between(dayStart, overlapEnd)
            startMinutes.toFloat() to endMinutes.toFloat()
        } else null
    }

    val totalMinutes = segments.sumOf { (it.second - it.first).toDouble() }.toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateText = if (date == LocalDate.now()) "오늘" else date.format(DateTimeFormatter.ofPattern("MM/dd"))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            if (segments.isNotEmpty()) {
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                val durationText = if (hours > 0) "${hours}시간 ${mins}분" else "${mins}분"
                Text(
                    text = "총 $durationText",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray800
                )
            } else {
                Text(
                    text = "데이터 없음",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray500
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 24h Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Gray300, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val fullWidth = this.maxWidth
                segments.forEach { (start, end) ->
                    val startOffset = fullWidth * (start / 1440f)
                    val segmentWidth = fullWidth * ((end - start) / 1440f)

                    Box(
                        modifier = Modifier
                            .offset(x = startOffset)
                            .width(segmentWidth)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

