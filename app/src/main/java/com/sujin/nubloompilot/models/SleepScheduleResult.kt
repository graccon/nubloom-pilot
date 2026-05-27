package com.sujin.nubloompilot.models

import java.time.LocalDateTime

/**
 * Data model representing the result of MCTQ-based sleep scheduling.
 */
data class SleepScheduleResult(
    val targetSleepStart: LocalDateTime,
    val targetSleepEnd: LocalDateTime,
    val targetSleepDurationMinutes: Int,
    val baselineSleepDurationMinutes: Int,
    val adjustmentReason: String
)
