package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.ShiftType
import com.sujin.nubloompilot.models.getTimeRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TargetSleepTimeCalculator {

    fun calculate(
        currentShift: ShiftType,
        workDate: LocalDate
    ): LocalDateTime {
        val shiftRange = currentShift.getTimeRange(workDate)

        return when (currentShift) {
            ShiftType.DAY -> {
                LocalDateTime.of(workDate, LocalTime.of(23, 0))
            }

            ShiftType.EVENING -> {
                shiftRange.endTime?.plusHours(1)
                    ?: LocalDateTime.of(workDate, LocalTime.of(23, 0))
            }

            ShiftType.NIGHT -> {
                shiftRange.endTime?.plusHours(1)
                    ?: LocalDateTime.of(workDate.plusDays(1), LocalTime.of(8, 30))
            }

            ShiftType.OFF -> {
                LocalDateTime.of(workDate, LocalTime.of(23, 0))
            }
        }
    }
}