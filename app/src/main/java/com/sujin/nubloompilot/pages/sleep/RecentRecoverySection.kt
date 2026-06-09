package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.ui.theme.Gray500

@Composable
fun RecentRecoverySection(
    latestSummary: DailyHealthSummary?,
    recentSummaries: List<DailyHealthSummary>,
    sleepSummariesLast24h: List<DailyHealthSummary>,
    checkInHistory: List<SleepResult>,
    averageSleepDurationMinutes: Long?,
    averageShiftSleepDurationMinutes: Long?,
    todayShift: String?,
    averageWakeHeartRate: Long?,
    isLoading: Boolean,
    isHistoryLoading: Boolean,
    isBaselineLoading: Boolean
) {
    var selectedSleepSummary by remember(sleepSummariesLast24h) {
        mutableStateOf(sleepSummariesLast24h.maxByOrNull { it.sleepDurationMinutes })
    }

    if (isLoading) {
        Text("데이터를 불러오는 중...")
    } else {
        // 1. Today's Specific Session Section (Conditional)
        if (latestSummary != null) {
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
        } else {
            // Today data missing state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "오늘 감지된 수면 데이터가 없습니다.\n갤럭시 워치를 착용하고 주무셨는지 확인해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Historical Data Section (Always Visible)
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
    }
}
