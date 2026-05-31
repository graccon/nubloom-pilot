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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.ui.theme.Blue
import com.sujin.nubloompilot.ui.theme.DarkRed
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Primary
import com.sujin.nubloompilot.ui.theme.SecondaryRed
import com.sujin.nubloompilot.ui.theme.SecondaryYellow

@Composable
fun SleepStageStackedBar(
    lightSleepMinutes: Long,
    deepSleepMinutes: Long,
    remSleepMinutes: Long,
    awakeSleepMinutes: Long,
    modifier: Modifier = Modifier
) {
    val totalMinutes = lightSleepMinutes + deepSleepMinutes + remSleepMinutes + awakeSleepMinutes

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "수면 단계 비율",
            style = MaterialTheme.typography.titleSmall,
            color = Gray800
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        if (totalMinutes == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Gray300, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "수면 단계 데이터가 없습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800
                )
            }
        } else {
            // Stacked Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (lightSleepMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(lightSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(SecondaryYellow)
                    )
                }
                if (deepSleepMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(deepSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(Primary)
                    )
                }
                if (remSleepMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(remSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(SecondaryRed)
                    )
                }
                if (awakeSleepMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .weight(awakeSleepMinutes.toFloat())
                            .fillMaxHeight()
                            .background(Blue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StageLegendItem(Modifier.weight(1f), "얕은 수면", lightSleepMinutes, SecondaryYellow)
                    StageLegendItem(Modifier.weight(1f), "깊은 수면", deepSleepMinutes, Primary)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    StageLegendItem(Modifier.weight(1f), "렘 수면", remSleepMinutes, SecondaryRed)
                    StageLegendItem(Modifier.weight(1f), "깨어있음", awakeSleepMinutes, Blue)
                }
            }
        }
    }
}

@Composable
private fun StageLegendItem(
    modifier: Modifier = Modifier,
    label: String,
    minutes: Long,
    color: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ${minutes}분",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = Gray800
        )
    }
}
