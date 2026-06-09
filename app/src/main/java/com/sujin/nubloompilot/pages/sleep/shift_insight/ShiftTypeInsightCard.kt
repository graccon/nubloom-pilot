package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.ShiftTypeInsight
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.models.ShiftInsightType
import com.sujin.nubloompilot.models.ShiftPatternInsight
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme

@Composable
fun ShiftTypeInsightCard(
    title: String,
    description: String,
    sectionATitle: String,
    sectionBTitle: String,
    sectionBThirdLabel: String,
    insight: ShiftTypeInsight?
) {
    ShiftInsightCard(
        title = title,
        description = description
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 섹션 A: regularPattern
        InsightPatternSection(
            title = sectionATitle,
            pattern = insight?.regularPattern,
            thirdLabel = "특징"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 섹션 B: transitionPattern
        InsightPatternSection(
            title = sectionBTitle,
            pattern = insight?.transitionPattern,
            thirdLabel = sectionBThirdLabel
        )
    }
}

// TODO :Preview
@Preview(
    name = "Shift Type Insight Card - Day",
    showBackground = true,
    widthDp = 360
)
// TODO :Preview
@Preview(
    name = "Shift Type Insight Card - Day",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun ShiftTypeInsightCardPreview() {
    val dummyInsight = ShiftTypeInsight(
        shiftType = ShiftInsightType.DAY,
        regularPattern = ShiftPatternInsight(
            sampleCount = 7,
            averageSleepDurationMinutes = 390L,
            typicalSleepDurationMinMinutes = 330L,
            typicalSleepDurationMaxMinutes = 450L,
            averageFatigueLevel = 2.8,
            morningGloryTypeCounts = mapOf(
                "TYPE_1" to 3,
                "TYPE_2" to 2,
                "TYPE_3" to 1,
                "TYPE_4" to 1
            ),
            featureText = "Day 근무일에는 비교적 일정한 시간대에 수면이 형성되는 경향이 있어요.",
            hasEnoughData = true
        ),
        transitionPattern = ShiftPatternInsight(
            sampleCount = 3,
            averageSleepDurationMinutes = 340L,
            typicalSleepDurationMinMinutes = 310L,
            typicalSleepDurationMaxMinutes = 390L,
            averageFatigueLevel = 3.7,
            morningGloryTypeCounts = mapOf(
                "TYPE_1" to 0,
                "TYPE_2" to 1,
                "TYPE_3" to 2,
                "TYPE_4" to 0
            ),
            featureText = "휴일 이후 Day 근무로 복귀할 때 수면 시간이 짧아지는 경향이 보여요.",
            hasEnoughData = false
        )
    )

    NubloomPilotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            ShiftTypeInsightCard(
                title = "Day 근무 인사이트",
                description = "Day 근무일의 평소 수면 패턴과 휴일 이후 복귀 패턴을 비교해요.",
                sectionATitle = "평소 Day 근무 패턴",
                sectionBTitle = "휴일 이후 Day 복귀 패턴",
                sectionBThirdLabel = "복귀 특징",
                insight = dummyInsight
            )
        }
    }
}