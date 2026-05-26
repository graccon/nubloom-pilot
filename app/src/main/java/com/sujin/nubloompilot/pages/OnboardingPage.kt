package com.sujin.nubloompilot.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.components.FloatingIcon
import com.sujin.nubloompilot.components.PrimaryButton
import com.sujin.nubloompilot.components.QuestionTextField
import com.sujin.nubloompilot.components.SpeechBubble
import com.sujin.nubloompilot.models.BaselineAssessment
import com.sujin.nubloompilot.models.MCTQShiftResponse
import com.sujin.nubloompilot.models.ShiftType
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import kotlinx.coroutines.launch

private enum class OnboardingStep {
    DEMOGRAPHICS,
    PERSONALIZATION,
    MCTQ_PLACEHOLDER
}

data class MctqFormState(
    val bedTime: String = "",
    val tryToSleepTime: String = "",
    val sleepLatency: String = "",
    val wakeUpTime: String = "",
    val alarmUsed: Boolean = false,
    val outOfBedLatency: String = "",
    val napTaken: Boolean = false,
    val napStartTime: String = "",
    val napEndTime: String = "",
    val canChooseSleepFreely: Boolean = true,
    val reasonIfCannotChoose: String = ""
)

@Composable
fun OnboardingPage(
    onSubmit: (
        name: String,
        birthYear: Int,
        assessment: BaselineAssessment
    ) -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.DEMOGRAPHICS) }
    val scope = rememberCoroutineScope()
    val mctqScrollState = rememberScrollState()

    val mctqBlocks = remember {
        listOf(
            MctqSleepBlockInfo(
                title = "이틀 연속 주간근무 시 중간수면",
                subtitle = "주간근무 D - 수면* - 주간근무 D"
            ),
            MctqSleepBlockInfo(
                title = "주간근무 이후 이틀간의 휴일 시 수면",
                subtitle = "주간근무 D - 휴일 - 수면* - 휴일"
            ),
            MctqSleepBlockInfo(
                title = "이틀 연속 오후근무 시 중간수면",
                subtitle = "오후근무 E - 수면* - 오후근무 E"
            ),
            MctqSleepBlockInfo(
                title = "오후근무 이후 이틀간의 휴일 시 수면",
                subtitle = "오후근무 E - 휴일 - 수면* - 휴일"
            ),
            MctqSleepBlockInfo(
                title = "이틀 연속 야간근무 시 중간수면",
                subtitle = "야간근무 N - 수면* - 야간근무 N"
            ),
            MctqSleepBlockInfo(
                title = "야간근무 이후 이틀간의 휴일 시 수면",
                subtitle = "야간근무 N - 휴일 - 수면* - 휴일"
            )
        )
    }

    var currentMctqBlockIndex by remember { mutableIntStateOf(0) }

    var mctqFormStates by remember {
        mutableStateOf(List(6) { MctqFormState() })
    }

    // Demographics data
    var name by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }

    // Personalization data
    var commuteTime by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }

    val currentMctqState = mctqFormStates[currentMctqBlockIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 32.dp)
    ) {
        if (currentStep != OnboardingStep.MCTQ_PLACEHOLDER) {
            Spacer(modifier = Modifier.height(60.dp))
            FloatingIcon(
                iconRes = R.drawable.icon_flower
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(targetState = currentStep, label = "OnboardingContent") { step ->
                when (step) {
                    OnboardingStep.DEMOGRAPHICS -> DemographicsStep(
                        name = name,
                        onNameChange = { name = it },
                        birthYear = birthYear,
                        onBirthYearChange = {
                            if (it.length <= 4 && it.all(Char::isDigit)) {
                                birthYear = it
                            }
                        }
                    )
                    OnboardingStep.PERSONALIZATION -> PersonalizationStep(
                        commuteTime = commuteTime,
                        onCommuteTimeChange = { if (it.all(Char::isDigit)) commuteTime = it },
                        prepTime = prepTime,
                        onPrepTimeChange = { if (it.all(Char::isDigit)) prepTime = it }
                    )
                    OnboardingStep.MCTQ_PLACEHOLDER -> Column {
                        Text(
                            text = "${currentMctqBlockIndex + 1} / ${mctqBlocks.size}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        MctqSleepForm(
                            blockInfo = mctqBlocks[currentMctqBlockIndex],
                            scrollState = mctqScrollState,
                            bedTime = currentMctqState.bedTime,
                            onBedTimeChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(bedTime = newValue)
                                }
                            },
                            tryToSleepTime = currentMctqState.tryToSleepTime,
                            onTryToSleepTimeChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(tryToSleepTime = newValue)
                                }
                            },
                            sleepLatency = currentMctqState.sleepLatency,
                            onSleepLatencyChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(sleepLatency = newValue)
                                }
                            },
                            wakeUpTime = currentMctqState.wakeUpTime,
                            onWakeUpTimeChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(wakeUpTime = newValue)
                                }
                            },
                            alarmUsed = currentMctqState.alarmUsed,
                            onAlarmUsedChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(alarmUsed = newValue)
                                }
                            },
                            outOfBedLatency = currentMctqState.outOfBedLatency,
                            onOutOfBedLatencyChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(outOfBedLatency = newValue)
                                }
                            },
                            napTaken = currentMctqState.napTaken,
                            onNapTakenChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(napTaken = newValue)
                                }
                            },
                            napStartTime = currentMctqState.napStartTime,
                            onNapStartTimeChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(napStartTime = newValue)
                                }
                            },
                            napEndTime = currentMctqState.napEndTime,
                            onNapEndTimeChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(napEndTime = newValue)
                                }
                            },
                            canChooseSleepFreely = currentMctqState.canChooseSleepFreely,
                            onCanChooseSleepFreelyChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(canChooseSleepFreely = newValue)
                                }
                            },
                            reasonIfCannotChoose = currentMctqState.reasonIfCannotChoose,
                            onReasonIfCannotChooseChange = { newValue ->
                                mctqFormStates = mctqFormStates.toMutableList().also {
                                    it[currentMctqBlockIndex] = it[currentMctqBlockIndex].copy(reasonIfCannotChoose = newValue)
                                }
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep != OnboardingStep.DEMOGRAPHICS) {
                OutlinedButton(
                    onClick = {
                        when (currentStep) {
                            OnboardingStep.PERSONALIZATION -> {
                                currentStep = OnboardingStep.DEMOGRAPHICS
                            }
                            OnboardingStep.MCTQ_PLACEHOLDER -> {
                                if (currentMctqBlockIndex > 0) {
                                    currentMctqBlockIndex--
                                    scope.launch { mctqScrollState.scrollTo(0) }
                                } else {
                                    currentStep = OnboardingStep.PERSONALIZATION
                                }
                            }
                            else -> Unit
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("이전")
                }
            }

            val isNextEnabled = when (currentStep) {
                OnboardingStep.DEMOGRAPHICS -> name.isNotBlank() && birthYear.length == 4
                OnboardingStep.PERSONALIZATION -> commuteTime.isNotBlank() && prepTime.isNotBlank()
                OnboardingStep.MCTQ_PLACEHOLDER -> true
            }

            val isLastBlock = currentStep == OnboardingStep.MCTQ_PLACEHOLDER && 
                            currentMctqBlockIndex == mctqBlocks.size - 1

            PrimaryButton(
                text = if (isLastBlock) "시작할게요" else "다음",
                onClick = {
                    when (currentStep) {
                        OnboardingStep.DEMOGRAPHICS -> currentStep = OnboardingStep.PERSONALIZATION
                        OnboardingStep.PERSONALIZATION -> {
                            currentStep = OnboardingStep.MCTQ_PLACEHOLDER
                            currentMctqBlockIndex = 0
                        }
                        OnboardingStep.MCTQ_PLACEHOLDER -> {
                            if (currentMctqBlockIndex < mctqBlocks.size - 1) {
                                currentMctqBlockIndex++
                                scope.launch { mctqScrollState.scrollTo(0) }
                            } else {
                                val mctqResponses = mctqFormStates.mapIndexed { index, state ->
                                    val shiftType = when (index) {
                                        0, 1 -> ShiftType.DAY
                                        2, 3 -> ShiftType.EVENING
                                        else -> ShiftType.NIGHT
                                    }
                                    val isWorkday = index % 2 == 0

                                    MCTQShiftResponse(
                                        shiftType = shiftType,
                                        isWorkday = isWorkday,
                                        bedTime = state.bedTime,
                                        tryToSleepTime = state.tryToSleepTime,
                                        sleepLatencyMinutes = state.sleepLatency.toIntOrNull() ?: 0,
                                        wakeUpTime = state.wakeUpTime,
                                        alarmUsed = state.alarmUsed,
                                        outOfBedLatencyMinutes = state.outOfBedLatency.toIntOrNull() ?: 0,
                                        napTaken = state.napTaken,
                                        napStartTime = if (state.napTaken) state.napStartTime else null,
                                        napEndTime = if (state.napTaken) state.napEndTime else null,
                                        canChooseSleepFreely = state.canChooseSleepFreely,
                                        reasonIfCannotChoose = if (state.canChooseSleepFreely) null else state.reasonIfCannotChoose
                                    )
                                }
                                val assessment = BaselineAssessment(
                                    commuteMinutes = commuteTime.toIntOrNull() ?: 0,
                                    preWorkPreparationMinutes = prepTime.toIntOrNull() ?: 0,
                                    mctqResponses = mctqResponses
                                )
                                onSubmit(name, birthYear.toInt(), assessment)
                            }
                        }
                    }
                },
                enabled = isNextEnabled,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
private fun DemographicsStep(
    name: String,
    onNameChange: (String) -> Unit,
    birthYear: String,
    onBirthYearChange: (String) -> Unit
) {
    var isInputFocused by remember { mutableStateOf(false) }
    Column {
        if (!isInputFocused) {
            Text(
                text = "안녕하세요, 간호사님",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            SpeechBubble(
                text = "실험에 참여해주셔서 감사합니다"
            )
            Spacer(modifier = Modifier.height(12.dp))
            SpeechBubble(
                text = "시작하기 전에 간단한 정보가 필요해요"
            )
            Spacer(modifier = Modifier.height(32.dp))
       }

        QuestionTextField(
            question = "이름을 입력해주세요",
            value = name,
            onValueChange = onNameChange,
            placeholder = "예) 홍길동",
            modifier = Modifier.onFocusChanged {
                isInputFocused = it.isFocused
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        QuestionTextField(
            question = "출생년도를 입력해주세요",
            value = birthYear,
            onValueChange = onBirthYearChange,
            placeholder = "예) 1990",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.onFocusChanged {
                isInputFocused = it.isFocused
            }
        )
    }
}

@Composable
private fun PersonalizationStep(
    commuteTime: String,
    onCommuteTimeChange: (String) -> Unit,
    prepTime: String,
    onPrepTimeChange: (String) -> Unit
) {
    var isInputFocused by remember { mutableStateOf(false) }
    val titleSize by animateFloatAsState(
        targetValue = if (isInputFocused) 24f else 36f,
        label = "titleSize"
    )

    Column {
        Text(
            text = "생활 패턴 알기",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = titleSize.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!isInputFocused) {
            SpeechBubble(
                text = "더 정확한 개입을 위해 필요해요"
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        QuestionTextField(
            question = "평균 통근 시간은 몇 분인가요?",
            value = commuteTime,
            onValueChange = onCommuteTimeChange,
            placeholder = "예) 45",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.onFocusChanged {
                isInputFocused = it.isFocused
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        QuestionTextField(
            question = "출근 준비 시간은 몇 분인가요?",
            value = prepTime,
            onValueChange = onPrepTimeChange,
            placeholder = "예) 60",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.onFocusChanged {
                isInputFocused = it.isFocused
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPagePreview() {
    NubloomPilotTheme {
        OnboardingPage(
            onSubmit = { _, _, _ -> }
        )
    }
}
