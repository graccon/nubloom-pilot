package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import com.sujin.nubloompilot.repository.ShiftAroundToday
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object SleepInterventionContextBuilder {

    fun getObjectiveRecoveryLevel(type: MorningGloryType): Int {
        return when (type) {
            MorningGloryType.TYPE_1 -> 5
            MorningGloryType.TYPE_2 -> 2
            MorningGloryType.TYPE_3 -> 4
            MorningGloryType.TYPE_4 -> 1
        }
    }

    fun build(
        type: MorningGloryType,
        fatigueLevel: Int,
        endTime: String,
        shiftsAroundToday: ShiftAroundToday,
        chronotype: Chronotype = Chronotype.INTERMEDIATE,
        workDate: LocalDate = LocalDate.now(),
        commuteMinutes: Long = 60L,
        preWorkPreparationMinutes: Long = 60L,
        mctqBaselineProfile: MctqBaselineProfile? = null,
        mctqBehaviorProfile: MctqBehaviorProfile? = null
    ): SleepInterventionContext {
        val objectiveRecoveryLevel = getObjectiveRecoveryLevel(type)
        
        val currentShift = ShiftType.fromString(shiftsAroundToday.todayShift)
        val nextShift = ShiftType.fromString(shiftsAroundToday.tomorrowShift)
        val previousShift = ShiftType.fromString(shiftsAroundToday.yesterdayShift)

        val mainSleepDuration = MainSleepDurationCalculator.calculate(
            currentShift = currentShift,
            previousShift = previousShift,
            nextShift = nextShift,
            subjectiveFatigueLevel = fatigueLevel,
            objectiveRecoveryLevel = objectiveRecoveryLevel,
            chronotype = chronotype
        )

        val targetSleepTime = TargetSleepTimeCalculator.calculate(
            currentShift = currentShift,
            nextShift = nextShift,
            workDate = workDate,
            mainSleepDurationMinutes = mainSleepDuration.toMinutes(),
            commuteMinutes = commuteMinutes,
            preWorkPreparationMinutes = preWorkPreparationMinutes
        )

        val wakeTime = (if (endTime == "NONE") Instant.now() else Instant.parse(endTime))
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        return SleepInterventionContext(
            chronotype = chronotype,
            previousShift = previousShift,
            currentShift = currentShift,
            nextShift = nextShift,
            workDate = workDate,
            wakeTime = wakeTime,
            targetSleepTime = targetSleepTime,
            subjectiveFatigueLevel = fatigueLevel,
            objectiveRecoveryLevel = objectiveRecoveryLevel,
            mctqBaselineProfile = mctqBaselineProfile,
            mctqBehaviorProfile = mctqBehaviorProfile
        )
    }
}
