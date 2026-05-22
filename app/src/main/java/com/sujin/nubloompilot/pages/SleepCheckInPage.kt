package com.sujin.nubloompilot.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.SpeechBubbleCard
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepInterpretationInput
import com.sujin.nubloompilot.ui.theme.*
import com.sujin.nubloompilot.utils.MorningGloryClassifier
import com.sujin.nubloompilot.utils.SleepInterpretationEngine

@Composable
fun SleepCheckInPage(
    participantName: String,
    sleepDurationMinutes: Long,
    wakeHeartRate: Long?,
    baselineSleepDurationMinutes: Long?,
    baselineWakeHeartRate: Long?,
    onSubmitClick: (MorningGloryType) -> Unit,
    modifier: Modifier = Modifier
){
    var sleepTimePerception by remember { mutableStateOf<Int?>(null) }
    var fatigueLevel by remember { mutableStateOf<Int?>(null) }

    val canSubmit = sleepTimePerception != null && fatigueLevel != null

    val buttonText = when {
        !canSubmit -> "모든 항목을 선택해주세요"
        else -> "오늘의 나팔꽃 확인하기"
    }

    val interpretation = remember(
        sleepDurationMinutes,
        wakeHeartRate,
        baselineSleepDurationMinutes,
        baselineWakeHeartRate
    ) {
        SleepInterpretationEngine.interpret(
            SleepInterpretationInput(
                sleepDurationMinutes = sleepDurationMinutes,
                wakeHeartRate = wakeHeartRate,
                baselineSleepDurationMinutes = baselineSleepDurationMinutes,
                baselineWakeHeartRate = baselineWakeHeartRate
            )
        )
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2B2B2B), // top
                    Gray900           // bottom
                )

            ))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SleepReportHeader(
            participantName = participantName
        )

        SpeechBubbleCard {
            BubbleTag(text = "수면 시간")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = interpretation.sleepDuration.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = interpretation.sleepDuration.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(
                thickness = (1.5).dp,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Q. 실제로 느끼기엔 어떠셨나요?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val options = listOf(
                    Triple("덜 잔 것 같다", R.drawable.sleep_bad, 0),
                    Triple("비슷하다", R.drawable.sleep_normal, 1),
                    Triple("더 잔 것 같다", R.drawable.sleep_good, 2)
                )

                options.forEach { (label, imageRes, index) ->
                    SelectableOption(
                        label = label,
                        imageRes = imageRes,
                        isSelected = sleepTimePerception == index,
                        onClick = { sleepTimePerception = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        SpeechBubbleCard {
            BubbleTag(text = "수면 회복")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = interpretation.recovery.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = interpretation.recovery.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(
                thickness = (1.5).dp,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Q. 지금 몸 상태는 어떤가요?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            FatigueLevelSelector(
                selectedLevel = fatigueLevel,
                onSelect = { fatigueLevel = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val resultType = MorningGloryClassifier.classify(
                    interpretation = interpretation,
                    fatigueLevel = fatigueLevel!!
                )
                onSubmitClick(resultType)
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Gray400
            ),
            shape = RoundedCornerShape(46.dp)
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        }
    }
}

@Composable
private fun SelectableOption(
    label: String,
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val optionShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .height(96.dp)
            .clip(optionShape)
            .background(
                color = if (isSelected) HighlightsYellow else Color.White
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) Gray800 else Gray500,
                shape = optionShape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
                maxLines = 2
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SleepCheckInPagePreview() {
    NubloomPilotTheme {
        SleepCheckInPage(
            participantName = "간호사",
            sleepDurationMinutes = 450L,
            wakeHeartRate = 65L,
            baselineSleepDurationMinutes = null,
            baselineWakeHeartRate = null,
            onSubmitClick = {}
        )
    }
}

@Composable
private fun SleepReportHeader(
    participantName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Text(
            text = "오늘의 수면 리포트",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = HighlightsYellow
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$participantName 선생님, 오늘 수면은 어떤가요?\n수면 데이터와 몸이 느끼는 상태는 다를 수 있어요",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray200
        )

        Spacer(modifier = Modifier.height(18.dp))
    }
}


@Composable
fun BubbleTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Gray500,
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 11.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray900
        )
    }
}

@Composable
private fun FatigueLevelSelector(
    selectedLevel: Int?,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        "아주 맑음, 완전히 깨어있음",
        "매우 활기차지만 최고조는 아님",
        "괜찮음, 약간 개운함",
        "조금 피곤함, 덜 개운함",
        "중간 정도로 피곤함, 처짐",
        "극도로 피곤함, 집중하기 매우 어려움",
        "완전히 방전됨, 기능 수행 불가능"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            FatigueCheckOption(
                number = index + 1,
                label = label,
                isSelected = selectedLevel == index,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun FatigueCheckOption(
    number: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                color = if (isSelected) HighlightsYellow else Gray100,
                shape = shape
            )
            .border(
                width = 1.3.dp,
                color = if (isSelected) Gray800 else Gray500,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray800,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray800,
            modifier = Modifier.weight(1f)
        )
    }
}