package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray800

@Composable
fun SleepDurationComparisonCard(
    title: String = "오늘 총 수면시간",
    todayAllSleepDurationMinutes: Long,
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
                label = title,
                value = formatDuration(todaySleepDurationMinutes),
                labelStyle = MaterialTheme.typography.titleMedium,
                valueStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            SleepMetricRow(
                label = "오늘 총 수면시간 (24h)",
                value = formatDuration(todayAllSleepDurationMinutes),
                labelStyle = MaterialTheme.typography.bodyLarge,
                valueStyle = MaterialTheme.typography.bodyLarge
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
fun ShiftSleepComparisonCard(
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
            Text(
                text = "$shiftLabel 근무 최근 3회",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
fun SleepSummaryInfoCard(
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

private fun formatDuration(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}
