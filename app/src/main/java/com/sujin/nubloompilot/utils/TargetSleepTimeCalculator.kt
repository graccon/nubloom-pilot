package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.shared.models.ShiftTimingConfig
import com.sujin.nubloompilot.shared.models.ShiftType
import com.sujin.nubloompilot.shared.models.getTimeRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TargetSleepTimeCalculator {
    fun calculate(
        currentShift: ShiftType,
        nextShift: ShiftType?,
        workDate: LocalDate,
        mainSleepDurationMinutes: Long = 7 * 60L,
        commuteMinutes: Long = 60L,
        preWorkPreparationMinutes: Long = 60L,
        shiftTimingConfig: ShiftTimingConfig = ShiftTimingConfig.Default
    ): LocalDateTime {
        val currentShiftRange = currentShift.getTimeRange(workDate, shiftTimingConfig)

        return when (currentShift) {
            ShiftType.DAY -> {
                calculateNightSleepBeforeNextShift(
                    workDate = workDate,
                    nextShift = nextShift,
                    commuteMinutes = commuteMinutes,
                    mainSleepDurationMinutes = mainSleepDurationMinutes,
                    preWorkPreparationMinutes = preWorkPreparationMinutes,
                    fallbackTime = LocalTime.of(23, 0),
                    shiftTimingConfig = shiftTimingConfig
                )
            }

            ShiftType.EVENING -> {
                currentShiftRange.endTime
                    ?.plusMinutes(commuteMinutes + preWorkPreparationMinutes)
                    ?: LocalDateTime.of(workDate.plusDays(1), LocalTime.of(0, 30))
            }

            ShiftType.NIGHT -> {
                currentShiftRange.endTime
                    ?.plusMinutes(commuteMinutes + preWorkPreparationMinutes)
                    ?: LocalDateTime.of(workDate.plusDays(1), LocalTime.of(8, 30))
            }

            ShiftType.OFF -> {
                calculateNightSleepBeforeNextShift(
                    workDate = workDate,
                    nextShift = nextShift,
                    mainSleepDurationMinutes = mainSleepDurationMinutes,
                    commuteMinutes = commuteMinutes,
                    preWorkPreparationMinutes = preWorkPreparationMinutes,
                    fallbackTime = LocalTime.of(23, 0),
                    shiftTimingConfig = shiftTimingConfig
                )
            }
        }
    }

    private fun calculateNightSleepBeforeNextShift(
        workDate: LocalDate,
        nextShift: ShiftType?,
        commuteMinutes: Long,
        preWorkPreparationMinutes: Long,
        mainSleepDurationMinutes: Long,
        fallbackTime: LocalTime,
        shiftTimingConfig: ShiftTimingConfig
    ): LocalDateTime {
        val nextWorkDate = workDate.plusDays(1)
        val nextShiftStart = nextShift
            ?.getTimeRange(nextWorkDate, shiftTimingConfig)
            ?.startTime
            ?: return LocalDateTime.of(workDate, fallbackTime)

        return when (nextShift) {
            ShiftType.DAY, ShiftType.EVENING -> {
                nextShiftStart
                    .minusMinutes(commuteMinutes)
                    .minusMinutes(commuteMinutes)
                    .minusMinutes(preWorkPreparationMinutes)
                    .minusMinutes(mainSleepDurationMinutes)
            }

            ShiftType.NIGHT -> {
                LocalDateTime.of(workDate.plusDays(1), LocalTime.of(0, 30))
            }

            ShiftType.OFF, null -> {
                LocalDateTime.of(workDate, fallbackTime)
            }
        }
    }
}