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
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800
import java.time.YearMonth

@Composable
fun ShiftScheduleEditor(
    currentYearMonth: YearMonth,
    shifts: Map<Int, String>,
    isEditMode: Boolean,
    selectedDay: Int,

    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onEditStart: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onDaySelect: (Int) -> Unit,
    onShiftSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "듀티표",
                style = MaterialTheme.typography.displayMedium
            )

            if (isEditMode) {
                Row {
                    IconButton(onClick = onCancelEdit) {
                        Icon(Icons.Default.Close, null)
                    }

                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Check, null)
                    }
                }
            } else {
                IconButton(onClick = onEditStart) {
                    Icon(Icons.Default.Edit, null)
                }
            }
        }

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
            onDayClick = {
                if (isEditMode) {
                    onDaySelect(it)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Gray800,
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        Gray600,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "근무를 선택해주세요",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                ShiftTypeSelector(
                    onShiftSelected = onShiftSelect
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!isEditMode) {
            ShiftLegend()
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}