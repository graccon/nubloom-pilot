package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.Chronotype
import com.sujin.nubloompilot.models.ShiftType
import java.time.Duration

object MainSleepDurationCalculator {

    fun calculate(
        currentShift: ShiftType,
        nextShift: ShiftType?,
        previousShift: ShiftType?,
        subjectiveFatigueLevel: Int,
        objectiveRecoveryLevel: Int,
        chronotype: Chronotype
    ): Duration {
        var hours = when (currentShift) {
            ShiftType.NIGHT -> 7.0
            ShiftType.EVENING -> 7.0
            ShiftType.DAY -> 6.5
            ShiftType.OFF -> 7.0
        }

        if (objectiveRecoveryLevel <= 2) {
            hours += 1.0
        }

        if (subjectiveFatigueLevel >= 4) {
            hours += 0.5
        }

        if (
            currentShift == ShiftType.NIGHT &&
            nextShift == ShiftType.NIGHT
        ) {
            hours += 0.5
        }

        if (
            currentShift == ShiftType.NIGHT &&
            previousShift != ShiftType.NIGHT
        ) {
            hours += 0.5
        }

        if (
            chronotype == Chronotype.MORNING &&
            currentShift == ShiftType.NIGHT
        ) {
            hours += 0.5
        }

        val clampedHours = hours.coerceIn(6.0, 9.0)
        val minutes = (clampedHours * 60).toLong()

        return Duration.ofMinutes(minutes)
    }
}