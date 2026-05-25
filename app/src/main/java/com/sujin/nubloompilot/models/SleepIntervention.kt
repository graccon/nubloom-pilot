package com.sujin.nubloompilot.models

import java.time.LocalDate
import java.time.LocalDateTime

data class SleepIntervention(
    val type: InterventionType,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val title: String,
    val description: String,
    val reason: String,
    val actionType: InterventionActionType
)

enum class InterventionType {
    MAIN_SLEEP,
    CAFFEINE,
    NAP,
    LIGHT
}

enum class InterventionActionType {
    DO,
    AVOID
}

data class SleepInterventionContext(
    val chronotype: Chronotype,
    val previousShift: ShiftType?,
    val currentShift: ShiftType,
    val nextShift: ShiftType?,
    val workDate: LocalDate,
    val wakeTime: LocalDateTime,
    val targetSleepTime: LocalDateTime,
    val subjectiveFatigueLevel: Int,
    val objectiveRecoveryLevel: Int
)