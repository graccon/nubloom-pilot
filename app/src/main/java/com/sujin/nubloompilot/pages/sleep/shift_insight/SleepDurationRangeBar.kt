package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800

@Composable
fun SleepDurationRangeBar(
    averageMinutes: Long?,
    rangeMinMinutes: Long?,
    rangeMaxMinutes: Long?,
    sampleCount: Int,
    maxMinutes: Long = 720L // 12시간 기준
) {
    val averagePosition = if (averageMinutes != null) {
        (averageMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }

    val rangeStartPosition = if (rangeMinMinutes != null) {
        (rangeMinMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }

    val rangeEndPosition = if (rangeMaxMinutes != null) {
        (rangeMaxMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }

    val hasRange = rangeStartPosition != null &&
            rangeEndPosition != null &&
            rangeEndPosition >= rangeStartPosition

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "평균 수면 시간 (범위)",
                style = MaterialTheme.typography.labelMedium,
                color = Gray700
            )

            Text(
                text = formatMinutesToHourMinute(averageMinutes),
                style = MaterialTheme.typography.labelMedium,
                color = Gray800
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            // 전체 0~12h 기준 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Gray300)
            )

            // 0부터 평균 수면 시간까지 채우는 바
            if (averagePosition != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(averagePosition)
                        .height(16.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Gray500)
                )
            }

            // 범위 시작 짧은 세로 바
            if (hasRange) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.Center)

                ) {
                    val startX = rangeStartPosition!! * size.width
                    val endX = rangeEndPosition!! * size.width
                    val centerY = size.height / 2f
                    drawLine(
                        color = Gray800,
                        start = Offset(startX, centerY),
                        end = Offset(endX, centerY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(1f, 13f),
                            phase = 0f
                        )
                    )

                }
                SleepDurationMarker(
                    position = rangeStartPosition!!,
                    width = 3.dp,
                    height = 14.dp,
                    color = Gray800
                )

                // 범위 끝 짧은 세로 바
                SleepDurationMarker(
                    position = rangeEndPosition!!,
                    width = 3.dp,
                    height = 14.dp,
                    color = Gray800
                )
            }

            // 평균 수면 시간 긴 세로 바
            if (averagePosition != null) {
                SleepDurationMarker(
                    position = averagePosition,
                    width = 4.dp,
                    height = 32.dp,
                    color = Gray800
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0h",
                style = MaterialTheme.typography.labelSmall,
                color = Gray700
            )

            Text(
                text = "12h",
                style = MaterialTheme.typography.labelSmall,
                color = Gray700
            )
        }

        if (hasRange) {
            SleepDurationSummaryText(
                averageMinutes = averageMinutes,
                rangeMinMinutes = rangeMinMinutes,
                rangeMaxMinutes = rangeMaxMinutes,
                hasRange = hasRange
            )
        }

    }
}

@Composable
private fun SleepDurationSummaryText(
    averageMinutes: Long?,
    rangeMinMinutes: Long?,
    rangeMaxMinutes: Long?,
    hasRange: Boolean
) {
    Column(
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Text(
            text = "이 근무에서는 평균 ${formatMinutesToHourMinute(averageMinutes)} 정도 주무셨어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800
        )

        Text(
            text = if (hasRange) {
                "주로 ${formatMinutesToHourMinute(rangeMinMinutes)} ~ ${formatMinutesToHourMinute(rangeMaxMinutes)} 사이로 수면 시간이 형성됐어요. 이 범위가 좁을수록 수면 시간이 비교적 일정하게 유지된 것으로 볼 수 있어요."
            } else {
                "수면 시간 범위를 보려면 기록이 조금 더 필요해요."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun BoxScope.SleepDurationMarker(
    position: Float,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .align(Alignment.CenterStart),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier.weight(position.coerceAtLeast(0.001f))
        )

        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(width / 2))
                .background(color)
        )

        Spacer(
            modifier = Modifier.weight((1f - position).coerceAtLeast(0.001f))
        )
    }
}