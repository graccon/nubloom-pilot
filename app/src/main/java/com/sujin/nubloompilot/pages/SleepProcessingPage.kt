package com.sujin.nubloompilot.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.sujin.nubloompilot.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.components.SpriteAnimation
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepIntervention
import com.sujin.nubloompilot.models.SleepInterventionContext
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.ui.theme.*
import com.sujin.nubloompilot.utils.SleepInterventionEngine
import kotlinx.coroutines.delay
import java.time.Instant

private enum class SleepProcessingStep {
    UNDERSTANDING_DATA,
    SELECTING_SLEEP_PLAN,
    GENERATING_INTERVENTIONS
}

private data class SleepPlanCandidate(
    val id: String,
    val title: String,
    val description: String,
    val hasNap: Boolean
)

@Composable
fun SleepProcessingPage(
    participantId: String,
    participantName: String,
    type: MorningGloryType,
    endTime: String,
    duration: Long,
    heartRate: Long?,
    fatigueLevel: Int,
    interventionContext: SleepInterventionContext,
    onSaveResult: suspend (SleepResult) -> Unit,
    onSaveInterventions: suspend (List<SleepIntervention>, SleepInterventionContext) -> Unit,
    onProcessingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(SleepProcessingStep.UNDERSTANDING_DATA) }
    var selectedCandidate by remember { mutableStateOf<SleepPlanCandidate?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val candidates = remember {
        listOf(
            SleepPlanCandidate(
                id = "long_main_sleep",
                title = "낮잠 없이 긴 수면",
                description = "낮잠 없이 한 번의 긴 메인 수면으로 회복하는 계획이에요.",
                hasNap = false
            ),
            SleepPlanCandidate(
                id = "nap_and_short_sleep",
                title = "낮잠 + 짧은 수면",
                description = "낮잠으로 피로를 나누고, 메인 수면을 조금 짧게 가져가는 계획이에요.",
                hasNap = true
            )
        )
    }

    LaunchedEffect(currentStep) {
        when (currentStep) {
            SleepProcessingStep.UNDERSTANDING_DATA -> {
                errorMessage = null
                val startTime = System.currentTimeMillis()
                try {
                    val result = SleepResult(
                        participantId = participantId,
                        participantName = participantName,
                        sleepEndTime = Instant.parse(endTime),
                        sleepDurationMinutes = duration,
                        wakeHeartRate = heartRate,
                        fatigueLevel = fatigueLevel,
                        morningGloryType = type
                    )
                    onSaveResult(result)
                    
                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = 4000L - elapsed
                    if (remaining > 0) delay(remaining)
                    
                    currentStep = SleepProcessingStep.SELECTING_SLEEP_PLAN
                } catch (e: Exception) {
                    errorMessage = "데이터 저장 중 오류가 발생했습니다: ${e.message}"
                }
            }
            SleepProcessingStep.GENERATING_INTERVENTIONS -> {
                errorMessage = null
                val startTime = System.currentTimeMillis()
                try {
                    val interventions = SleepInterventionEngine.generate(interventionContext)
                    onSaveInterventions(interventions, interventionContext)

                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = 3000L - elapsed
                    if (remaining > 0) delay(remaining)

                    onProcessingComplete()
                } catch (e: Exception) {
                    errorMessage = "회복 계획 생성 중 오류가 발생했습니다: ${e.message}"
                }
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Gray800,
                        Gray900,
                        Gray900
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ProcessingContent(
            step = currentStep,
            candidates = candidates,
            selectedCandidate = selectedCandidate,
            errorMessage = errorMessage,
            onCandidateSelect = { selectedCandidate = it },
            onConfirmSelection = { currentStep = SleepProcessingStep.GENERATING_INTERVENTIONS },
            onRetry = { currentStep = SleepProcessingStep.UNDERSTANDING_DATA }
        )
    }
}

@Composable
private fun ProcessingContent(
    step: SleepProcessingStep,
    candidates: List<SleepPlanCandidate>,
    selectedCandidate: SleepPlanCandidate?,
    errorMessage: String?,
    onCandidateSelect: (SleepPlanCandidate) -> Unit,
    onConfirmSelection: () -> Unit,
    onRetry: () -> Unit
) {
    if (errorMessage != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
        return
    }

    val stepIndex = when (step) {
        SleepProcessingStep.UNDERSTANDING_DATA -> 1
        SleepProcessingStep.SELECTING_SLEEP_PLAN -> 2
        SleepProcessingStep.GENERATING_INTERVENTIONS -> 3
    }

    val pageTitle = "오늘의 회복 계획 ($stepIndex / 3)"

    val title = when (step) {
        SleepProcessingStep.UNDERSTANDING_DATA -> "선생님의 오늘 상태를 살펴보고 있어요..."
        SleepProcessingStep.SELECTING_SLEEP_PLAN -> "오늘은 어떤 수면이 더 잘 맞을까요?"
        SleepProcessingStep.GENERATING_INTERVENTIONS -> "선생님의 회복 계획을 생성하고 있어요..."
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(62.dp))
        Text(
            text = pageTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = HighlightsYellow,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )


        if (step != SleepProcessingStep.SELECTING_SLEEP_PLAN) {
            Spacer(modifier = Modifier.weight(1f))
            SpriteAnimation(
                frames = listOf(
                    R.drawable.loading_1,
                    R.drawable.loading_2,
                    R.drawable.loading_3,
                    R.drawable.loading_4,
                    R.drawable.loading_5,
                    R.drawable.loading_4,
                    R.drawable.loading_3,
                    R.drawable.loading_2,
                ),
                modifier = Modifier.size(84.dp),
                frameDuration = 180L
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        
        if (step == SleepProcessingStep.SELECTING_SLEEP_PLAN) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                candidates.forEach { candidate ->
                    SleepPlanCard(
                        candidate = candidate,
                        isSelected = selectedCandidate?.id == candidate.id,
                        onClick = { onCandidateSelect(candidate) }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onConfirmSelection,
                enabled = selectedCandidate != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighlightsYellow,
                    disabledContainerColor = Gray700
                ),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = if (selectedCandidate != null) "${selectedCandidate.title} 할래요" else "계획을 선택해주세요",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SleepPlanCard(
    candidate: SleepPlanCandidate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) HighlightsYellow.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) HighlightsYellow else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = candidate.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) HighlightsYellow else Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = candidate.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SleepProcessingPagePreview() {
    val dummyContext = SleepInterventionContext(
        chronotype = com.sujin.nubloompilot.models.Chronotype.INTERMEDIATE,
        previousShift = com.sujin.nubloompilot.models.ShiftType.OFF,
        currentShift = com.sujin.nubloompilot.models.ShiftType.DAY,
        nextShift = com.sujin.nubloompilot.models.ShiftType.DAY,
        workDate = java.time.LocalDate.now(),
        wakeTime = java.time.LocalDateTime.now(),
        targetSleepTime = java.time.LocalDateTime.now().plusHours(16),
        subjectiveFatigueLevel = 3,
        objectiveRecoveryLevel = 3
    )
    NubloomPilotTheme {
        SleepProcessingPage(
            participantId = "test",
            participantName = "테스트",
            type = MorningGloryType.TYPE_1,
            endTime = Instant.now().toString(),
            duration = 480L,
            heartRate = 70L,
            fatigueLevel = 3,
            interventionContext = dummyContext,
            onSaveResult = {},
            onSaveInterventions = { _, _ -> },
            onProcessingComplete = {}
        )
    }
}
