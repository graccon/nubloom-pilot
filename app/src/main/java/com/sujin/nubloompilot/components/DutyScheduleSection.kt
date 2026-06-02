package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.shared.models.ShiftTimingConfig
import com.sujin.nubloompilot.ui.theme.*
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun DutyScheduleSection(
    currentYearMonth: YearMonth,
    shifts: Map<Int, String>,
    isEditMode: Boolean,
    selectedDay: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onEditStart: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onDayClick: (Int) -> Unit,
    onShiftSelected: (String) -> Unit,
    timingConfig: ShiftTimingConfig,
    onTimingConfigChange: (ShiftTimingConfig) -> Unit,
    onResetTimingConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        DutyScheduleHeader(
            isEditMode = isEditMode,
            onEditStart = onEditStart,
            onCancelEdit = onCancelEdit,
            onSave = onSave
        )

        Spacer(modifier = Modifier.height(4.dp))

        MonthNavigator(
            year = currentYearMonth.year,
            month = currentYearMonth.monthValue,
            onPrevClick = onPrevMonth,
            onNextClick = onNextMonth
        )

        Spacer(modifier = Modifier.height(1.dp))

        ShiftCalendarGrid(
            year = currentYearMonth.year,
            month = currentYearMonth.monthValue,
            shifts = shifts,
            selectedDay = if (isEditMode) selectedDay else null,
            onDayClick = { day ->
                if (isEditMode) {
                    onDayClick(day)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditMode) {
            DutyScheduleEditPanel(
                onShiftSelected = onShiftSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShiftTimingEditSection(
                config = timingConfig,
                onConfigChange = onTimingConfigChange,
                onReset = onResetTimingConfig
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!isEditMode) {
            ShiftLegend(config = timingConfig)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DutyScheduleHeader(
    isEditMode: Boolean,
    onEditStart: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "듀티표",
            style = MaterialTheme.typography.displaySmall
        )

        if (isEditMode) {
            Row {
                IconButton(onClick = onCancelEdit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "수정 취소"
                    )
                }

                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "저장"
                    )
                }
            }
        } else {
            IconButton(onClick = onEditStart) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "수정"
                )
            }
        }
    }
}

@Composable
private fun DutyScheduleEditPanel(
    onShiftSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Gray700,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = Gray400,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "해당 일의 근무를 선택해주세요",
            style = MaterialTheme.typography.bodyLarge,
//            color = Gray100
        )

        Spacer(modifier = Modifier.height(16.dp))

        ShiftTypeSelector(
            onShiftSelected = onShiftSelected
        )
    }
}

@Composable
private fun ShiftTimingEditSection(
    config: ShiftTimingConfig,
    onConfigChange: (ShiftTimingConfig) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Gray700,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = Gray300,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "근무 시간 기준",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "D/E/N 근무가 실제로 시작되는 시간을 설정해주세요. 이 시간은 수면 일정 계산과 타임라인 표시 기준으로 사용됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray700
        )

        Spacer(modifier = Modifier.height(16.dp))

        val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

        TimingInputField(
            label = "주간근무 (D) 시작",
            value = config.dayStart.format(timeFormatter),
            onValueChange = { newValue ->
                safeParseLocalTime(newValue)?.let {
                    onConfigChange(config.copy(dayStart = it))
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        TimingInputField(
            label = "오후근무 (E) 시작",
            value = config.eveningStart.format(timeFormatter),
            onValueChange = { newValue ->
                safeParseLocalTime(newValue)?.let {
                    onConfigChange(config.copy(eveningStart = it))
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        TimingInputField(
            label = "야간근무 (N) 시작",
            value = config.nightStart.format(timeFormatter),
            onValueChange = { newValue ->
                safeParseLocalTime(newValue)?.let {
                    onConfigChange(config.copy(nightStart = it))
                }
            }
        )

        androidx.compose.material3.TextButton(
            onClick = onReset,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("기본값으로 되돌리기",
                color = Gray700,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun TimingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    // 부모의 value가 변경되면 (예: 리셋) 로컬 상태도 동기화
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    val isError = textFieldValue.text.isNotEmpty() && textFieldValue.text.length < 5

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Column(
            horizontalAlignment = Alignment.End
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = textFieldValue,
                onValueChange = { input ->
                    val formatted = formatTimeInput(input.text)
                    textFieldValue = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )

                    if (formatted.length == 5) {
                        onValueChange(formatted)
                    }
                },
                modifier = Modifier
                    .width(100.dp)
                    .height(54.dp),
                singleLine = true,
                isError = isError,
                placeholder = {
                    Text(
                        text = "06:30",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Gray500
                )
            )

            if (isError) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "숫자 4자리 입력",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTimeInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)
    if (digits.isEmpty()) return ""

    val hourDigits = digits.take(2)
    val minuteDigits = digits.drop(2)

    val safeHour = hourDigits.toIntOrNull()?.coerceIn(0, 23)?.let {
        if (hourDigits.length == 2) "%02d".format(it) else it.toString()
    } ?: hourDigits

    val safeMinute = if (minuteDigits.length == 2) {
        minuteDigits.toIntOrNull()?.coerceIn(0, 59)?.let {
            "%02d".format(it)
        } ?: minuteDigits
    } else {
        minuteDigits
    }

    return if (minuteDigits.isEmpty()) {
        safeHour
    } else {
        "$safeHour:$safeMinute"
    }
}

private fun safeParseLocalTime(timeStr: String): LocalTime? {
    return try {
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            val hour = parts[0].trim().toInt().coerceIn(0, 23)
            val minute = parts[1].trim().toInt().coerceIn(0, 59)
            LocalTime.of(hour, minute)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
