package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.ui.theme.Gray300
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SelectedSleepSummarySection(
    summary: DailyHealthSummary,
    total24hSleepMinutes: Long,
    hasMultipleSleepSummaries: Boolean,
    averageSleepDurationMinutes: Long?,
    averageShiftSleepDurationMinutes: Long?,
    todayShift: String?,
    averageWakeHeartRate: Long?,
    isBaselineLoading: Boolean
) {
    val startTimeText = summary.sleepStartTime.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    val endTimeText = summary.sleepEndTime.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    Column {
        SleepDurationComparisonCard(
            title = if (hasMultipleSleepSummaries) "선택된 수면의 시간" else "오늘 총 수면시간",
            todayAllSleepDurationMinutes = total24hSleepMinutes,
            todaySleepDurationMinutes = summary.sleepDurationMinutes,
            averageSleepDurationMinutes = if (isBaselineLoading) null else averageSleepDurationMinutes
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
            averageShiftSleepDurationMinutes = if (isBaselineLoading) null else averageShiftSleepDurationMinutes
        )
    }
}
