package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.RecoveryRhythmGraphData
import com.sujin.nubloompilot.models.ShiftInsightType
import androidx.compose.foundation.background
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray600
import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme

@Composable
fun RecoveryRhythmGraphCard(
    graphData: RecoveryRhythmGraphData?,
    focusedShift: ShiftInsightType,
    canvasContent: @Composable (Set<ShiftInsightType>) -> Unit
) {
    val visibleShifts = remember(focusedShift) { 
        if (focusedShift == ShiftInsightType.OFF) setOf(ShiftInsightType.OFF)
        else setOf(focusedShift, ShiftInsightType.OFF)
    }

    ShiftInsightCard(
        title = graphData?.title ?: "회복 리듬 데이터 준비 중",
        description = "${focusedShift.name} 근무 시 예상되는 회복 리듬입니다."
    ) {
        if (graphData != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 16.dp,
                    alignment = Alignment.End
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(label = "${focusedShift.name} 리듬", color = getSeriesColor(focusedShift).copy(alpha = 0.3f))
                if (focusedShift != ShiftInsightType.OFF) {
                    LegendItem(label = "${focusedShift.name} 근무", color = getSeriesColor(focusedShift))
                    LegendItem(label = "Off(기준) 리듬", color = getSeriesColor(ShiftInsightType.OFF).copy(alpha = 0.3f))
                }
            }
            
            canvasContent(visibleShifts)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = getRecoveryRhythmGuideText(focusedShift),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Gray800)
    }
}

// TODO: Preview
@Preview(showBackground = true, widthDp = 360)
@Composable
fun RecoveryRhythmGraphCardPreview() {
    val dummyGraphData = RecoveryRhythmGraphData(
        title = "근무 유형별 예상 회복 리듬",
        description = "DAY 근무 시 예상되는 회복 리듬입니다.",
        series = emptyList()
    )

    NubloomPilotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RecoveryRhythmGraphCard(
                graphData = dummyGraphData,
                focusedShift = ShiftInsightType.DAY,
                canvasContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Canvas Preview Area",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
            )
        }
    }
}

private fun getRecoveryRhythmGuideText(
    focusedShift: ShiftInsightType
): String {
    return if (focusedShift == ShiftInsightType.OFF) {
        "Off 리듬은 쉬는 날의 기준 회복 흐름이에요. " +
                "근무가 없는 날에는 수면과 회복 리듬이 비교적 자연스럽게 나타날 수 있어요. " +
                "이 흐름을 기준으로 Day, Evening, Night 근무일의 회복 리듬이 얼마나 달라지는지 비교할 수 있어요."
    } else {
        "Off 리듬은 쉬는 날의 기준 흐름이에요. " +
                "근무 리듬이 Off보다 높으면 회복이 더 잘 된 상태, 낮으면 회복이 부족한 상태로 볼 수 있어요. " +
                "곡선이 가파르게 변할수록 회복이나 피로 변화가 빠르다는 뜻이에요."
    }
}