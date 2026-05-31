package com.sujin.nubloompilot.models

import com.sujin.nubloompilot.shared.models.ShiftType

/**
 * Data model for the baseline assessment collected during onboarding.
 */
data class BaselineAssessment(
    val commuteMinutes: Int = 0,
    val preWorkPreparationMinutes: Int = 0,
    val mctqResponses: List<MCTQShiftResponse> = emptyList()
)

/**
 * Detailed sleep-wake pattern response for a specific shift type and day type (Workday/Free day).
 * Used in K-MCTQshift questionnaire.
 */
data class MCTQShiftResponse(
    val shiftType: ShiftType, // DAY, EVENING, NIGHT
    val isWorkday: Boolean,
    val bedTime: String,             // Format "HH:mm"
    val tryToSleepTime: String,      // Format "HH:mm"
    val sleepLatencyMinutes: Int,
    val wakeUpTime: String,          // Format "HH:mm"
    val alarmUsed: Boolean,
    val outOfBedLatencyMinutes: Int,
    val napTaken: Boolean,
    val napStartTime: String? = null, // Format "HH:mm"
    val napEndTime: String? = null,   // Format "HH:mm"
    val canChooseSleepFreely: Boolean,
    val reasonIfCannotChoose: String? = null
)
