package com.sujin.nubloompilot.models

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
    val wakeTime: LocalDateTime,
    val targetSleepTime: LocalDateTime,
    val workStartTime: LocalDateTime?,
    val workEndTime: LocalDateTime?,
    val subjectiveFatigueLevel: Int, // 1~5
    val objectiveRecoveryLevel: Int  // 1~5
)