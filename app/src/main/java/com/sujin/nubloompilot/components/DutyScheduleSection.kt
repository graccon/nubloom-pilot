package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray700
import java.time.YearMonth

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

        Spacer(modifier = Modifier.height(24.dp))

        MonthNavigator(
            year = currentYearMonth.year,
            month = currentYearMonth.monthValue,
            onPrevClick = onPrevMonth,
            onNextClick = onNextMonth
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditMode) {
            DutyScheduleEditPanel(
                onShiftSelected = onShiftSelected
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!isEditMode) {
            ShiftLegend()
            Spacer(modifier = Modifier.weight(1f))
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
            style = MaterialTheme.typography.displayMedium
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