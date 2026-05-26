package com.sujin.nubloompilot.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.components.SpriteAnimation
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import java.time.LocalDate
import java.time.LocalDateTime
import com.sujin.nubloompilot.models.*
import com.sujin.nubloompilot.utils.MainSleepDurationCalculator
import com.sujin.nubloompilot.utils.SleepInterventionEngine
import com.sujin.nubloompilot.utils.TargetSleepTimeCalculator


@Composable
fun MorningGloryResultPage(
    participantName: String,
    type: MorningGloryType,
    onBackHome: () -> Unit,
    onSaveResult: suspend () -> Unit = {},
    onSaveInterventions: suspend (List<SleepIntervention>, SleepInterventionContext) -> Unit = { _, _ -> },
    interventionContext: SleepInterventionContext,
    isReviewMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val resultInfo = remember(type) {
        getMorningGloryResultInfo(type)
    }
    val interventions = remember(interventionContext) {
        SleepInterventionEngine.generate(interventionContext)
    }

    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Gray100,
                        Gray100,
                        Gray100,
                        Gray100,
                        Gray600,
                        Gray900
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        ResultHeader(
//            participantName = participantName
//        )
//        Spacer(modifier = Modifier.height(20.dp))
//
//        ResultMainMessage(
//            title = resultInfo.title,
//            description = resultInfo.description,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//
//        SpriteAnimation(
//            frames = resultInfo.frames,
//            frameDuration = resultInfo.frameDuration,
//            modifier = Modifier.size(260.dp)
//        )

        ResultSummaryCard(
            objectiveText = resultInfo.objectiveText,
            subjectiveText = resultInfo.subjectiveText,
            encouragementText = resultInfo.encouragementText,
            interventions = interventions
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isReviewMode) {
                    onBackHome()
                } else {
                    scope.launch {
                        isSaving = true
                        onSaveResult()
                        onSaveInterventions(interventions, interventionContext)
                        onBackHome()
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Gray400
            ),
            shape = RoundedCornerShape(46.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Gray900,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "홈으로 돌아가기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
            }
        }
    }
}

@Composable
private fun ResultHeader(
    participantName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Text(
            text = "오늘의 수면 나팔꽃이 폈어요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$participantName 선생님의 오늘 상태를 확인했어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray900
        )
    }
}

@Composable
private fun ResultMainMessage(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TypeHeader(
            typeName = title
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray900,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TypeHeader(
    typeName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        color = Gray100,
        border = BorderStroke(
            width = (1.2).dp,
            color = Gray400
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = typeName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultSummaryCard(
    objectiveText: String,
    subjectiveText: String,
    encouragementText: String,
    interventions: List<SleepIntervention>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE8E8E8),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {

        if (interventions.isNotEmpty()) {
            Text(
                text = "오늘의 추천 개입",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(12.dp))

            interventions.forEach { intervention ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = intervention.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${intervention.startTime.toLocalTime()} ~ ${intervention.endTime.toLocalTime()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = intervention.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray900
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

//        Text(
//            text = "오늘의 해석",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            color = Gray900
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Text(
//            text = objectiveText,
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.Medium,
//            color = Gray900
//        )
//
//        Spacer(modifier = Modifier.height(6.dp))
//
//        Text(
//            text = subjectiveText,
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.Medium,
//            color = Gray900
//        )
//
//        Spacer(modifier = Modifier.height(6.dp))
//
//        Text(
//            text = encouragementText,
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.Medium,
//            color = Gray900
//        )
    }
}

private data class MorningGloryResultInfo(
    val title: String,
    val description: String,
    val objectiveText: String,
    val subjectiveText: String,
    val frames: List<Int>,
    val encouragementText: String,
    val frameDuration: Long = 200L
)

private fun getMorningGloryResultInfo(
    type: MorningGloryType
): MorningGloryResultInfo {
    return when (type) {
        MorningGloryType.TYPE_1 -> MorningGloryResultInfo(
            title = "힘찬 나팔 불어라, 나팔꽃 !",
            description = "수면 데이터와 현재 컨디션이 모두 비교적 안정적인 상태예요.오늘은 몸의 리듬이 크게 흔들리지 않아, 평소처럼 활동을 시작하기 좋아 보여요.",
            objectiveText = "수면 시간과 회복 지표가 전반적으로 안정적인 흐름을 보이고 있어요.",
            subjectiveText = "스스로 느끼는 몸 상태도 무리가 크지 않고, 하루를 시작할 준비가 된 상태에 가까워요.",
            encouragementText = "좋은 흐름이에요. 오늘의 리듬을 그대로 이어가볼까요?",
            frames = listOf(
                R.drawable.type_1_1,
                R.drawable.type_1_2,
                R.drawable.type_1_3,
                R.drawable.type_1_4,
                R.drawable.type_1_3,
                R.drawable.type_1_2
            )
        )

        MorningGloryType.TYPE_2 -> MorningGloryResultInfo(
            title = "가볍게 깨어난 나팔꽃 !",
            description = "지금 몸은 괜찮게 느껴질 수 있지만, 수면 데이터상 회복은 충분하지 않을 수 있어요.\n초반 컨디션만 믿고 무리하면 오후나 근무 후반에 피로가 커질 수 있어요.",
            objectiveText = "수면 데이터에서는 회복이 조금 더 필요한 신호가 나타나고 있어요.",
            subjectiveText = "다만 현재 느끼는 몸 상태는 비교적 괜찮아, 피로를 바로 크게 느끼지는 않을 수 있어요.",
            encouragementText = "오늘은 초반부터 에너지를 많이 쓰기보다, 중간중간 짧게 쉬면서 페이스를 조절해봐요.",
            frames = listOf(
                R.drawable.type_2_1,
                R.drawable.type_2_2,
                R.drawable.type_2_3,
                R.drawable.type_2_4,
                R.drawable.type_2_5,
            )
        )

        MorningGloryType.TYPE_3 -> MorningGloryResultInfo(
            title = "덜 깬 나팔꽃 !",
            description = "수면 데이터는 비교적 괜찮지만, 현재 몸은 아직 피로를 느끼고 있어요.\n기록상으로는 나쁘지 않아도, 실제 컨디션이 따라오지 않는 날일 수 있어요.",
            objectiveText = "수면 데이터는 전반적으로 안정적인 범위에 있어, 회복 흐름 자체는 나쁘지 않아 보여요.",
            subjectiveText = "하지만 몸이 무겁거나 개운하지 않게 느껴진다면, 주관적 피로 신호를 함께 고려할 필요가 있어요.",
            encouragementText = "데이터보다 몸의 감각이 더 크게 느껴지는 날이에요. 오늘은 천천히 몸을 깨우면서 시작해봐요.",
            frames = listOf(
                R.drawable.type_3_1,
                R.drawable.type_3_2,
                R.drawable.type_3_3,
                R.drawable.type_3_4,
            )
        )

        MorningGloryType.TYPE_4 -> MorningGloryResultInfo(
            title = "웅크린 나팔꽃 !",
            description = "수면 데이터와 현재 몸 상태 모두 회복이 더 필요한 상태에 가까워요.\n오늘은 컨디션을 끌어올리기보다, 피로가 더 쌓이지 않도록 조절하는 것이 중요해요.",
            objectiveText = "수면 데이터에서 회복 부족 신호가 나타나고 있어, 충분한 휴식이 필요해 보여요.",
            subjectiveText = "스스로 느끼는 몸 상태도 피로에 가까워, 집중력이나 활력이 평소보다 낮을 수 있어요.",
            encouragementText = "오늘은 무리하지 않는 것이 가장 좋은 관리예요. 가능하다면 휴식, 수분 섭취, 가벼운 스트레칭부터 시작해봐요.",
            frames = listOf(
                R.drawable.type_4_1,
                R.drawable.type_4_2,
                R.drawable.type_4_3,
                R.drawable.type_4_4,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MorningGloryResultPagePreview() {
    val workDate = LocalDate.now()
    val chronotype = Chronotype.INTERMEDIATE
    val previousShift = ShiftType.OFF
    val currentShift = ShiftType.NIGHT
    val nextShift = ShiftType.NIGHT
    val fatigueLevel = 4
    val recoveryLevel = 4
    val commuteMinutes = 60L
    val preWorkPreparationMinutes = 60L

    val mainSleepDuration = MainSleepDurationCalculator.calculate(
        currentShift = currentShift,
        previousShift = previousShift,
        nextShift = nextShift,
        subjectiveFatigueLevel = fatigueLevel,
        objectiveRecoveryLevel = recoveryLevel,
        chronotype = chronotype
    )

    NubloomPilotTheme {
        MorningGloryResultPage(
            participantName = "간호사",
            onBackHome = {},
            type = MorningGloryType.TYPE_3,
            interventionContext = SleepInterventionContext(
                chronotype = chronotype,
                previousShift = previousShift,
                currentShift = currentShift,
                nextShift = nextShift,
                workDate = workDate,
                wakeTime = LocalDateTime.now()
                    .withHour(9)
                    .withMinute(0),
                targetSleepTime = TargetSleepTimeCalculator.calculate(
                    currentShift = currentShift,
                    nextShift = nextShift,
                    workDate = workDate,
                    mainSleepDurationMinutes = mainSleepDuration.toMinutes(),
                    commuteMinutes = commuteMinutes,
                    preWorkPreparationMinutes = preWorkPreparationMinutes
                ),
                subjectiveFatigueLevel = fatigueLevel,
                objectiveRecoveryLevel = recoveryLevel
            )
        )
    }
}