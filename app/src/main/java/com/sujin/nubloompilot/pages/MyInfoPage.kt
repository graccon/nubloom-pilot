package com.sujin.nubloompilot.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.sujin.nubloompilot.components.DutyScheduleSection
import com.sujin.nubloompilot.components.TopBanner
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import kotlinx.coroutines.delay
import java.time.YearMonth

@Composable
fun MyInfoPage(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduleRepository = remember {
        ShiftScheduleRepository(context)
    }

    var currentYearMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    var shifts by remember(currentYearMonth) {
        mutableStateOf(
            scheduleRepository.getLocalSchedule(
                year = currentYearMonth.year,
                month = currentYearMonth.monthValue
            )
        )
    }

    var originalShifts by remember(currentYearMonth) {
        mutableStateOf(shifts)
    }

    var showSavedBanner by remember {
        mutableStateOf(false)
    }

    var isEditMode by remember {
        mutableStateOf(false)
    }

    var selectedDay by remember(currentYearMonth) {
        mutableStateOf(1)
    }

    val daysInMonth = currentYearMonth.lengthOfMonth()

    LaunchedEffect(showSavedBanner) {
        if (showSavedBanner) {
            delay(2000)
            showSavedBanner = false
        }
    }

    Box(
        modifier = modifier
    ) {
        DutyScheduleSection(
            currentYearMonth = currentYearMonth,
            shifts = shifts,
            isEditMode = isEditMode,
            selectedDay = selectedDay,
            onPrevMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
            onEditStart = {
                originalShifts = shifts
                selectedDay = 1
                isEditMode = true
            },
            onCancelEdit = {
                shifts = originalShifts
                isEditMode = false
            },
            onSave = {
                scheduleRepository.saveSchedule(
                    year = currentYearMonth.year,
                    month = currentYearMonth.monthValue,
                    shifts = shifts,
                    onSuccess = {
                        originalShifts = shifts
                        isEditMode = false
                        showSavedBanner = true
                    },
                    onFailure = {
                        // TODO: 실패 배너 처리
                    }
                )
            },
            onDayClick = { day ->
                selectedDay = day
            },
            onShiftSelected = { shift ->
                shifts = shifts.toMutableMap().apply {
                    this[selectedDay] = shift
                }
                selectedDay = if (selectedDay < daysInMonth) {
                    selectedDay + 1
                } else {
                    1
                }
            }
        )

        TopBanner(
            visible = showSavedBanner,
            message = "듀티표가 저장되었습니다",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}