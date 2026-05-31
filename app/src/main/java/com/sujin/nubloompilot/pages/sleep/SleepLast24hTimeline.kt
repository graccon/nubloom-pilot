package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray800
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun SleepLast24hTimeline(
    summaries: List<DailyHealthSummary>,
    selectedSummary: DailyHealthSummary?,
    onSummarySelected: (DailyHealthSummary) -> Unit,
    modifier: Modifier = Modifier,
    referenceTime: Instant = Instant.now()
) {
    val startTimeLimit = referenceTime.minus(24, ChronoUnit.HOURS)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "최근 24시간 수면 타임라인",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Gray300, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            val totalWidth = this.maxWidth
            
            summaries.forEach { summary ->
                // Clamp times to the 24h window
                val start = if (summary.sleepStartTime.isBefore(startTimeLimit)) startTimeLimit else summary.sleepStartTime
                val end = if (summary.sleepEndTime.isAfter(referenceTime)) referenceTime else summary.sleepEndTime
                
                if (start.isBefore(end)) {
                    val durationFromStart = ChronoUnit.MINUTES.between(startTimeLimit, start)
                    val durationTotal = ChronoUnit.MINUTES.between(start, end)
                    
                    val offsetFraction = durationFromStart.toFloat() / 1440f
                    val widthFraction = durationTotal.toFloat() / 1440f
                    
                    val isSelected = summary == selectedSummary
                    val barColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = totalWidth * offsetFraction)
                            .width(totalWidth * widthFraction)
                            .fillMaxHeight()
                            .background(barColor)
                            .clickable { onSummarySelected(summary) }
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("24시간 전", style = MaterialTheme.typography.labelSmall, color = Gray800)
            Text("현재", style = MaterialTheme.typography.labelSmall, color = Gray800)
        }
    }
}
