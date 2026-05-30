package com.sujin.nubloompilot.models

import java.time.Instant

data class DailyHealthSummary(
    val sleepStartTime: Instant,
    val sleepEndTime: Instant,
    val sleepDurationMinutes: Long,
    val deepSleepMinutes: Long,
    val wakeHeartRate: Long?,
    val averageHrvMillis: Int?,
    val stepsLast24Hours: Long
)