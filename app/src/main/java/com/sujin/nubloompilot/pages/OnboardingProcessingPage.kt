package com.sujin.nubloompilot.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sujin.nubloompilot.ui.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween


private const val STEP_DURATION = 10_000L

data class ProcessingStep(
    val title: String,
    val description: String,
    val keywords: List<String>
)

private val processingSteps = listOf(
    ProcessingStep(
        title = "설문 응답을 정리하고 있어요...",
        description = """
            입력해주신 수면 습관과 근무 패턴을 하나씩 정리하고 있어요.
            선생님의 생활 리듬을 정확하게 이해하는 첫 단계예요.
        """.trimIndent(),
        keywords = listOf(
            "취침 시간",
            "기상 시간",
            "근무 유형"
        )
    ),
    ProcessingStep(
        title = "수면 리듬 기준선을 계산하고 있어요...",
        description = """
            주간, 이브닝, 야간 근무에 따라 
            달라지는 수면 리듬을 계산하고 있어요. 
            평소의 기준 수면 패턴을 바탕으로 맞춤 개입이 시작돼요.
        """.trimIndent(),
        keywords = listOf(
            "생체 리듬",
            "크로노타입",
            "수면 기준"
        )
    ),
    ProcessingStep(
        title = "선생님에게 맞는 수면을 분석하고 있어요...",
        description = """
            잠드는 시간, 낮잠 습관, 수면 지연, 
            회복하기 어려워하는 근무 유형을 함께 살펴보고 있어요. 
            선생님만의 수면 행동 특성을 분석하는 중이에요.
        """.trimIndent(),
        keywords = listOf(
            "낮잠 습관",
            "수면 지연",
            "취약 근무"
        )
    ),
    ProcessingStep(
        title = "맞춤 개입을 준비하고 있어요...",
        description = """
            언제 자는 것이 좋을지, 
            빛·카페인·낮잠을 어떻게 활용하면 좋을지 준비하고 있어요.
            근무 일정에 맞는 회복 가이드를 곧 보여드릴게요.
        """.trimIndent(),
        keywords = listOf(
            "빛 노출",
            "카페인 타이밍",
            "낮잠 전략"
        )
    )
)

@Composable
fun OnboardingProcessingPage(
    onAction: suspend () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val actionJob = async {
                onAction()
            }

            processingSteps.forEachIndexed { index, _ ->
                currentStepIndex = index
                delay(STEP_DURATION)
            }

            actionJob.await()
            onComplete()

        } catch (e: Exception) {
            errorMessage = "등록 중 오류가 발생했습니다: ${e.message}"
        }
    }

    val currentStep = processingSteps[currentStepIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Gray100, Gray100, Primary),
                    radius = 1500f
                ),
            )
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {}
            ) {
                Text("확인")
            }
        } else {
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
                modifier = Modifier.size(120.dp),
                frameDuration = 180L
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProcessingStepContent(
                step = currentStep,
                currentStepIndex = currentStepIndex
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingProcessingPagePreview() {
    NubloomPilotTheme {
        OnboardingProcessingPage(
            onAction = {},
            onComplete = {}
        )
    }
}

@Composable
private fun ProcessingStepContent(
    step: ProcessingStep,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    var showTitle by remember(step) { mutableStateOf(false) }
    var showDescription by remember(step) { mutableStateOf(false) }
    var visibleKeywordCount by remember(step) { mutableIntStateOf(0) }

    LaunchedEffect(step) {
        visibleKeywordCount = 0

        showTitle = true

        delay(1000)
        showDescription = true

        delay(1000)
        visibleKeywordCount = 1

        delay(500)
        visibleKeywordCount = 2

        delay(500)
        visibleKeywordCount = 3
        delay(5500)
        showDescription = false
        showTitle = false
        visibleKeywordCount = 0
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "${currentStepIndex + 1} / ${processingSteps.size}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Gray600,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        val titleAlpha by animateFloatAsState(
            targetValue = if (showTitle) 1f else 0f,
            animationSpec = tween(900, easing = FastOutSlowInEasing),
            label = "titleAlpha"
        )

        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900.copy(alpha = titleAlpha),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        val descAlpha by animateFloatAsState(
            targetValue = if (showDescription) 1f else 0f,
            animationSpec = tween(900, easing = FastOutSlowInEasing),
            label = "descAlpha"
        )
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800.copy(alpha = descAlpha),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(34.dp))

        ProcessingKeywordRow(
            keywords = step.keywords,
            visibleKeywordCount = visibleKeywordCount
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun ProcessingKeywordRow(
    keywords: List<String>,
    visibleKeywordCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.wrapContentWidth()
            .height(36.dp)
    ) {
        keywords.forEachIndexed { index, keyword ->
            val visible = index < visibleKeywordCount

            val alpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = FastOutSlowInEasing
                ),
                label = "keywordAlpha"
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = Primary.copy(alpha = 0.4f * alpha)
            ) {
                Text(
                    text = "# $keyword",
                    color = Gray800.copy(alpha = alpha),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                )
            }
        }
    }
}
