package com.sujin.nubloompilot.models

import java.time.Instant

/**
 * Represents a merged sleep event that may consist of multiple fragments.
 */
data class SleepEpisode(
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long,
    val deepSleepMinutes: Long
)
