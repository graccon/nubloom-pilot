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
                LegendItem(label = "${focusedShift.name} 근무", color = getSeriesColor(focusedShift))
                if (focusedShift != ShiftInsightType.OFF) {
                    LegendItem(label = "Off(기준) 리듬", color = getSeriesColor(ShiftInsightType.OFF).copy(alpha = 0.3f))
                }
            }
            
            canvasContent(visibleShifts)

            Spacer(modifier = Modifier.height(18.dp))

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
