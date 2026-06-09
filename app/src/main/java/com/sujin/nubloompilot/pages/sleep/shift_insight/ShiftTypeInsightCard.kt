package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.ShiftTypeInsight

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
