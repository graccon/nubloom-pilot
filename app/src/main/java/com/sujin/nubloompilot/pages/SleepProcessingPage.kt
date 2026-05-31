package com.sujin.nubloompilot.pages


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.sujin.nubloompilot.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.SpriteAnimation
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepIntervention
import com.sujin.nubloompilot.models.SleepInterventionContext
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationScheduler
import com.sujin.nubloompilot.ui.theme.*
import com.sujin.nubloompilot.utils.SleepInterventionEngine
import kotlinx.coroutines.delay
import java.time.Instant

private enum class SleepProcessingStep {
    UNDERSTANDING_DATA,
    MATCHING_SLEEP_PATTERN,
    GENERATING_INTERVENTIONS
}

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
    healthSummaryRepository: com.sujin.nubloompilot.repository.HealthSummaryRepository,
    onSaveResult: suspend (SleepResult) -> Unit,
    onSaveInterventions: suspend (List<SleepIntervention>, SleepInterventionContext) -> Unit,
    onProcessingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(SleepProcessingStep.UNDERSTANDING_DATA) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(currentStep) {
        when (currentStep) {
            SleepProcessingStep.UNDERSTANDING_DATA -> {
                errorMessage = null
                val startTime = System.currentTimeMillis()
                try {
                    val summary = healthSummaryRepository.getLatestHealthSummary()
                    
                    val result = SleepResult(
                        participantId = participantId,
                        participantName = participantName,
                        sleepEndTime = Instant.parse(endTime),
                        sleepDurationMinutes = duration,
                        wakeHeartRate = heartRate,
                        fatigueLevel = fatigueLevel,
                        morningGloryType = type,
                        sleepSummary = summary
                    )
                    onSaveResult(result)
                    SleepCheckInNotificationScheduler.cancelScheduledCheckIn(context)
                    
                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = 4000L - elapsed
                    if (remaining > 0) delay(remaining)
                    
                    currentStep = SleepProcessingStep.MATCHING_SLEEP_PATTERN
                } catch (e: Exception) {
                    errorMessage = "데이터 저장 중 오류가 발생했습니다: ${e.message}"
                }
            }
            SleepProcessingStep.MATCHING_SLEEP_PATTERN -> {
                delay(4000L)
                currentStep = SleepProcessingStep.GENERATING_INTERVENTIONS
            }
            SleepProcessingStep.GENERATING_INTERVENTIONS -> {
                errorMessage = null
                val startTime = System.currentTimeMillis()
                try {
                    val interventions = SleepInterventionEngine.generate(interventionContext)
                    onSaveInterventions(interventions, interventionContext)

                    // Schedule sleep check-in notification based on main sleep end time
                    val mainSleepEndTime =
                        SleepInterventionEngine.findMainSleepEndTime(interventions)
                    mainSleepEndTime?.let { endTime ->
                        SleepCheckInNotificationScheduler.scheduleAfterMainSleep(context, endTime)
                    }

                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = 3000L - elapsed
                    if (remaining > 0) delay(remaining)

                    onProcessingComplete()
                } catch (e: Exception) {
                    errorMessage = "회복 계획 생성 중 오류가 발생했습니다: ${e.message}"
                }
            }
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
            errorMessage = errorMessage,
            onRetry = {
                currentStep = SleepProcessingStep.UNDERSTANDING_DATA
            }
        )
    }
}

@Composable
private fun ProcessingContent(
    step: SleepProcessingStep,
    errorMessage: String?,
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
        SleepProcessingStep.MATCHING_SLEEP_PATTERN -> 2
        SleepProcessingStep.GENERATING_INTERVENTIONS -> 3
    }

    val pageTitle = "오늘의 회복 계획 ($stepIndex / 3)"

    val title = when (step) {
        SleepProcessingStep.UNDERSTANDING_DATA -> "선생님의 오늘 상태를 살펴보고 있어요..."
        SleepProcessingStep.MATCHING_SLEEP_PATTERN -> "평소 수면 패턴과 근무표를 맞춰보고 있어요..."
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
        Spacer(modifier = Modifier.height(42.dp))
        Spacer(modifier = Modifier.weight(1f))
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
    val context = LocalContext.current
    val dummyRepo = remember { 
        com.sujin.nubloompilot.repository.HealthSummaryRepository(
            com.sujin.nubloompilot.repository.HealthConnectRepository(context)
        )
    }
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
            healthSummaryRepository = dummyRepo,
            onSaveResult = {},
            onSaveInterventions = { _, _ -> },
            onProcessingComplete = {}
        )
    }
}
