package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.ShiftInsightSummary
import com.sujin.nubloompilot.models.ShiftInsightType
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.pages.sleep.shift_insight.*
import com.sujin.nubloompilot.ui.theme.Gray700

@Composable
fun ShiftInsightPlaceholderSection(
    shiftInsightSummary: ShiftInsightSummary? = null
) {
    // 1. 통합 상태 관리: 현재 사용자가 선택한 근무 유형 (화면 전체 연동)
    var selectedShift by remember { mutableStateOf(ShiftInsightType.DAY) }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // 상단 제목 및 설명
        Column {
            Text(
                text = "근무별 인사이트",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "근무 유형을 선택하여 수면 패턴과 회복 리듬을 확인하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
        }

        // 2. 통합 근무 유형 셀렉터 (주요 컨트롤러)
        ShiftTypeSelector(
            selectedShift = selectedShift,
            onShiftSelected = { selectedShift = it }
        )

        // 3. 선택된 근무에 연동되는 리듬 그래프
        com.sujin.nubloompilot.pages.sleep.shift_insight.RecoveryRhythmGraphCard(
            graphData = shiftInsightSummary?.recoveryRhythmGraphData,
            focusedShift = selectedShift
        ) { visibleShifts ->
            RecoveryRhythmCanvas(
                graphData = shiftInsightSummary?.recoveryRhythmGraphData ?: return@RecoveryRhythmGraphCard,
                visibleShifts = visibleShifts,
                focusedShift = selectedShift,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // 4. 선택된 근무에 연동되는 상세 인사이트 카드
        when (selectedShift) {
            ShiftInsightType.DAY -> ShiftTypeInsightCard(
                title = "Day 근무",
                description = "이른 출근 전후의 수면 시간과 Off 이후 Day 복귀 패턴을 분석해요.",
                sectionATitle = "Day 근무 수면 패턴",
                sectionBTitle = "Off → Day 복귀 패턴",
                sectionBThirdLabel = "수면 준비 필요도",
                insight = shiftInsightSummary?.dayInsight
            )
            ShiftInsightType.EVENING -> ShiftTypeInsightCard(
                title = "Evening 근무",
                description = "늦은 퇴근 이후 수면 시작 시각과 Off 이후 Evening 복귀 패턴을 분석해요.",
                sectionATitle = "Evening 근무 수면 패턴",
                sectionBTitle = "Off → Evening 복귀 패턴",
                sectionBThirdLabel = "수면 준비 필요도",
                insight = shiftInsightSummary?.eveningInsight
            )
            ShiftInsightType.NIGHT -> ShiftTypeInsightCard(
                title = "Night 근무",
                description = "야간근무 후 낮 수면과 Off 이후 Night 복귀 패턴을 분석해요.",
                sectionATitle = "Night 근무 수면 패턴",
                sectionBTitle = "Off → Night 복귀 패턴",
                sectionBThirdLabel = "낮잠 필요도",
                insight = shiftInsightSummary?.nightInsight
            )
            ShiftInsightType.OFF -> ShiftTypeInsightCard(
                title = "Off / 쉬는 날",
                description = "쉬는 날의 회복 수면과 근무 후 Off로 넘어가는 회복 패턴을 분석해요.",
                sectionATitle = "Off 날 수면 패턴",
                sectionBTitle = "근무 후 Off 회복 패턴",
                sectionBThirdLabel = "추가 회복 필요도",
                insight = shiftInsightSummary?.offInsight
            )
        }

        // 요약 안내
        ShiftInsightCard(
            title = "근무별 수면 패턴 분석",
            description = "충분한 기록이 쌓이면 근무별 평균 수면 시간과 피로도, 회복 패턴을 더 정확하게 보여드릴 예정이에요."
        )
    }
}


