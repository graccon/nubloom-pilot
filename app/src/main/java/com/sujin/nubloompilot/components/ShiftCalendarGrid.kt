package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Primary
import java.time.DayOfWeek
import java.time.YearMonth

@Composable
fun ShiftCalendarGrid(
    year: Int,
    month: Int,
    shifts: Map<Int, String>,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")

    val yearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()

    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek
    val emptyBefore = firstDayOfWeek.toKoreanCalendarIndex()

    val totalCells = ((emptyBefore + daysInMonth + 6) / 7) * 7

    val calendarDays = List<Int?>(totalCells) { index ->
        val day = index - emptyBefore + 1
        if (day in 1..daysInMonth) day else null
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // weekday
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdays.forEach { weekday ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = weekday,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // calendar
        calendarDays.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .padding(3.dp)
                            .clickable(
                                enabled = day != null
                            ) {
                                day?.let(onDayClick)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            ShiftDayCell(
                                day = day,
                                shift = shifts[day] ?: "O",
                                selected = selectedDay == day
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DayOfWeek.toKoreanCalendarIndex(): Int {
    return when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
}

@Composable
private fun ShiftDayCell(
    day: Int,
    shift: String,
    selected: Boolean,

) {
    val backgroundColor = when (shift) {
        "D" -> Color(0xFFA9C9EA)
        "E" -> Color(0xFFF4A249)
        "N" -> Color(0xFFEAB0D6)
        else -> Gray300
    }

    val textColor = when (shift) {
        "D" -> Color(0xFF24496E)
        "E" -> Color(0xFF9A4E2A)
        "N" -> Color(0xFF8A2A8A)
        else -> Gray700
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Gray700
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 34.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 2.dp,
                            color = Gray700,
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shift,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
    }
}