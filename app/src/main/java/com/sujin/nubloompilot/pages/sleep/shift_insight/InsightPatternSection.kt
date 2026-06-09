package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.ShiftPatternInsight
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray800

@Composable
fun InsightPatternSection(
    title: String,
    pattern: ShiftPatternInsight?,
    thirdLabel: String
) {
    Column {
        InsightSubTitle(text = title)
        
        InsightRow(
            label = if (title.contains("복귀") || title.contains("회복")) "복귀 전 평균 수면 시간" else "평균 수면 시간",
            value = formatMinutesToHourMinute(pattern?.averageSleepDurationMinutes)
        )
        InsightRow(
            label = if (title.contains("복귀") || title.contains("회복")) "복귀일 평균 피로도" else "평균 피로도",
            value = formatFatigueLevel(pattern?.averageFatigueLevel)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "나팔꽃 타입 분포",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Gray800,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        MorningGloryDistributionRows(
            counts = pattern?.morningGloryTypeCounts ?: emptyMap(),
            totalCount = pattern?.sampleCount ?: 0
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        InsightRow(label = thirdLabel, value = pattern?.featureText ?: "데이터 준비 중")

        if (pattern != null && pattern.sampleCount > 0) {
            InsightSampleCount(pattern.sampleCount)
            if (!pattern.hasEnoughData) {
                InsightInsufficientWarning()
            }
        }
    }
}

@Composable
fun InsightSubTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Gray800,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun InsightSampleCount(count: Int) {
    Text(
        text = "분석 기록 ${count}건",
        style = MaterialTheme.typography.labelSmall,
        color = Gray500,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun InsightInsufficientWarning() {
    Text(
        text = "아직 분석 기록이 적어 참고용으로만 확인해주세요.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
        modifier = Modifier.padding(top = 2.dp)
    )
}
