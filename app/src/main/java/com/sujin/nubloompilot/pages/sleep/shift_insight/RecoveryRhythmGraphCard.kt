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
        description = "${focusedShift.name} 근무 시 예상되는 회복 리듬입니다. 진한 부분은 실제 근무 시간 타임라인을 의미합니다."
    ) {
        if (graphData != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
            canvasContent(visibleShifts)

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "아래 바는 평균 수면이 이루어지는 시간대를 의미해요.",
                style = MaterialTheme.typography.labelSmall,
                color = Gray600
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(label = "${focusedShift.name} 리듬", color = getSeriesColor(focusedShift))
                if (focusedShift != ShiftInsightType.OFF) {
                    LegendItem(label = "Off(기준) 리듬", color = getSeriesColor(ShiftInsightType.OFF))
                }
            }
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
