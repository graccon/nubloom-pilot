package com.sujin.nubloompilot.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.ui.theme.DarkRed
import com.sujin.nubloompilot.ui.theme.Gray100
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray900
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import com.sujin.nubloompilot.ui.theme.Primary
import androidx.compose.foundation.rememberScrollState
data class MctqSleepBlockInfo(
    val title: String,
    val subtitle: String
)
private val MctqInputHeight = 52.dp

@Composable
fun MctqSleepForm(
    blockInfo: MctqSleepBlockInfo,
    scrollState: ScrollState,
    bedTime: String,
    onBedTimeChange: (String) -> Unit,
    tryToSleepTime: String,
    onTryToSleepTimeChange: (String) -> Unit,
    sleepLatency: String,
    onSleepLatencyChange: (String) -> Unit,
    wakeUpTime: String,
    onWakeUpTimeChange: (String) -> Unit,
    alarmUsed: Boolean,
    onAlarmUsedChange: (Boolean) -> Unit,
    outOfBedLatency: String,
    onOutOfBedLatencyChange: (String) -> Unit,
    napTaken: Boolean,
    onNapTakenChange: (Boolean) -> Unit,
    napStartTime: String,
    onNapStartTimeChange: (String) -> Unit,
    napEndTime: String,
    onNapEndTimeChange: (String) -> Unit,
    canChooseSleepFreely: Boolean,
    onCanChooseSleepFreelyChange: (Boolean) -> Unit,
    reasonIfCannotChoose: String,
    onReasonIfCannotChooseChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "MCTQ 교대 근무자 설문지",
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "질문은 근무일과 휴일의 수면과 깨어있을 때의 행동에 대한 내용입니다. 현재 당신의 근무 시간을 고려해 답해주세요. 최근 4주를 기준으로 가능한 한 시간을 명확히 적어주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = blockInfo.title,
            style = MaterialTheme.typography.titleLarge,
            color = DarkRed,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        ScheduleBadgeRow(
            subtitle = blockInfo.subtitle
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Questions
        MctqQuestionCard(
            question = "1. 잠자리에 들어가는 시간",
            imageResList = listOf(R.drawable.mctq_1)
        ) {
            MctqTimeInput(
                value = bedTime,
                onValueChange = onBedTimeChange
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        MctqQuestionCard(
            question = "2. 실제 잠을 청하려고 불을 끄거나 눈을 감은 시간",
            imageResList = listOf(R.drawable.mctq_2, R.drawable.mctq_3)
        ) {
            MctqTimeInput(
                value = tryToSleepTime,
                onValueChange = onTryToSleepTimeChange
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        MctqQuestionCard(
            question = "3. 잠드는 데 걸리는 시간(분)",
            imageResList = listOf(R.drawable.mctq_4)
        ) {
            MctqMinuteInput(
                value = sleepLatency,
                onValueChange = onSleepLatencyChange
            )
        }


        Spacer(modifier = Modifier.height(14.dp))

        MctqQuestionCard(
            question = "4. 잠에서 깬 시간",
            imageResList = listOf(R.drawable.mctq_5)
        ) {
            MctqWakeUpInput(
                time = wakeUpTime,
                onTimeChange = onWakeUpTimeChange,
                alarmUsed = alarmUsed,
                onAlarmUsedChange = onAlarmUsedChange
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        MctqQuestionCard(
            question = "5. 잠을 깬 후 침대에서 나오는 데 걸린 시간",
            imageResList = listOf(R.drawable.mctq_6)
        ) {
            MctqMinuteInput(
                value = outOfBedLatency,
                onValueChange = onOutOfBedLatencyChange
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        MctqQuestionCard(
            question = "6. 낮잠 여부",
        ) {
            MctqNapInput(
                napTaken = napTaken,
                onNapTakenChange = onNapTakenChange,
                napStartTime = napStartTime,
                onNapStartTimeChange = onNapStartTimeChange,
                napEndTime = napEndTime,
                onNapEndTimeChange = onNapEndTimeChange
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        MctqQuestionCard(
            question = "7. 수면 시간을 자유롭게 선택할 수 있나요?"
        ) {
            MctqSleepFreedomInput(
                canChooseSleepFreely = canChooseSleepFreely,
                onCanChooseSleepFreelyChange = onCanChooseSleepFreelyChange,
                reasonIfCannotChoose = reasonIfCannotChoose,
                onReasonIfCannotChooseChange = onReasonIfCannotChooseChange
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun MctqSleepFreedomInput(
    canChooseSleepFreely: Boolean,
    onCanChooseSleepFreelyChange: (Boolean) -> Unit,
    reasonIfCannotChoose: String,
    onReasonIfCannotChooseChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        MctqYesNoRow(
            selected = canChooseSleepFreely,
            onSelectedChange = onCanChooseSleepFreelyChange,
            yesLabel = "자유로움",
            noLabel = "선택 불가"
        )

        if (!canChooseSleepFreely) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reasonIfCannotChoose,
                onValueChange = onReasonIfCannotChooseChange,
                placeholder = { Text("이유 (예: 아이 돌봄, 소음 등)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun MctqNapInput(
    napTaken: Boolean,
    onNapTakenChange: (Boolean) -> Unit,
    napStartTime: String,
    onNapStartTimeChange: (String) -> Unit,
    napEndTime: String,
    onNapEndTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        MctqYesNoRow(
            selected = napTaken,
            onSelectedChange = onNapTakenChange
        )

        if (napTaken) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MctqTimeInput(
                    value = napStartTime,
                    onValueChange = onNapStartTimeChange,
                    placeholder = "시작 HH:mm",
                    modifier = Modifier.weight(1f)
                )

                MctqTimeInput(
                    value = napEndTime,
                    onValueChange = onNapEndTimeChange,
                    placeholder = "종료 HH:mm",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MctqMinuteInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "예) 15"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        placeholder = { Text(placeholder) },
        suffix = {
            Text(
                text = "분",
                color = Gray800,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = modifier.fillMaxWidth().height(MctqInputHeight),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun MctqQuestionImage(
    imageResList: List<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageResList.forEach { imageRes ->
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
            )
        }
    }
}

@Composable
fun MctqTimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "HH:mm"
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    val periodLabel = remember(textFieldValue.text) {
        if (textFieldValue.text.length == 5) {
            getTimePeriodLabel(textFieldValue.text)
        } else {
            ""
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { input ->
            val formatted = formatTimeInput(input.text)

            textFieldValue = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length)
            )

            onValueChange(formatted)
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingIcon = {
            if (periodLabel.isNotBlank()) {
                Text(
                    text = periodLabel,
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(MctqInputHeight),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

private fun getTimePeriodLabel(time: String): String {
    val hour = time.substringBefore(":").toIntOrNull() ?: return ""

    return when (hour) {
        in 0..5 -> "새벽"
        in 6..11 -> "아침"
        in 12..17 -> "오후"
        in 18..23 -> "밤"
        else -> ""
    }
}

@Composable
private fun MctqQuestionCard(
    question: String,
    imageResList: List<Int> = emptyList(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Gray100,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Gray400,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Gray900
        )
        if (imageResList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            MctqQuestionImage(
                imageResList = imageResList
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun MctqYesNoRow(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    yesLabel: String = "예",
    noLabel: String = "아니오"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        MctqOptionButton(
            label = yesLabel,
            isSelected = selected,
            onClick = { onSelectedChange(true) },
            modifier = Modifier.weight(1f)
        )
        MctqOptionButton(
            label = noLabel,
            isSelected = !selected,
            onClick = { onSelectedChange(false) },
            modifier = Modifier.weight(1f)
        )
    }
}


private fun formatTimeInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)

    if (digits.isEmpty()) return ""

    val hourDigits = digits.take(2)
    val minuteDigits = digits.drop(2)

    val safeHour = hourDigits
        .toIntOrNull()
        ?.coerceIn(0, 23)
        ?.toString()
        ?.padStart(hourDigits.length, '0')
        ?: hourDigits

    val safeMinute = if (minuteDigits.length == 2) {
        minuteDigits
            .toIntOrNull()
            ?.coerceIn(0, 59)
            ?.toString()
            ?.padStart(2, '0')
            ?: minuteDigits
    } else {
        minuteDigits
    }

    return if (minuteDigits.isEmpty()) {
        safeHour
    } else {
        "$safeHour:$safeMinute"
    }
}

@Composable
private fun MctqOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Primary else Gray500,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) Primary else Gray800,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun MctqWakeUpInput(
    time: String,
    onTimeChange: (String) -> Unit,
    alarmUsed: Boolean,
    onAlarmUsedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MctqTimeInput(
            value = time,
            onValueChange = onTimeChange,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = alarmUsed,
                onCheckedChange = onAlarmUsedChange
            )

            Text(
                text = "알람 사용",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
private fun ScheduleBadgeRow(
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val items = remember(subtitle) {
        subtitle.split(" - ")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, rawItem ->

            val isSleepBlock = rawItem.contains("*")
            val displayText = rawItem.replace("*", "")

            Surface(
                color = if (isSleepBlock) {
                    Primary.copy(alpha = 0.3f)
                } else {
                    Gray100
                },
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSleepBlock) {
                        Primary.copy(alpha = 0.8f)
                    } else {
                        Gray400
                    }
                )
            ) {
                Text(
                    text = displayText,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSleepBlock) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                    color = if (isSleepBlock) {
                        DarkRed
                    } else {
                        Gray800
                    }
                )
            }

            if (index != items.lastIndex) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MctqSleepFormFullPreview() {
    NubloomPilotTheme {
        MctqSleepForm(
            blockInfo = MctqSleepBlockInfo(
                title = "이틀 연속 주간근무 시 중간수면",
                subtitle = "주간근무 D - 수면* - 주간근무 D"
            ),
            scrollState = rememberScrollState(),
            bedTime = "22:30",
            onBedTimeChange = {},
            tryToSleepTime = "23:00",
            onTryToSleepTimeChange = {},
            sleepLatency = "15",
            onSleepLatencyChange = {},
            wakeUpTime = "06:30",
            onWakeUpTimeChange = {},
            alarmUsed = true,
            onAlarmUsedChange = {},
            outOfBedLatency = "10",
            onOutOfBedLatencyChange = {},
            napTaken = true,
            onNapTakenChange = {},
            napStartTime = "14:00",
            onNapStartTimeChange = {},
            napEndTime = "14:30",
            onNapEndTimeChange = {},
            canChooseSleepFreely = false,
            onCanChooseSleepFreelyChange = {},
            reasonIfCannotChoose = "근무 일정과 생활 패턴 때문에 자유롭게 선택하기 어렵습니다.",
            onReasonIfCannotChooseChange = {}
        )
    }
}