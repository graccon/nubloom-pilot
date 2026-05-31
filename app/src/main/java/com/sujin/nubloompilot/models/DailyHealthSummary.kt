package com.sujin.nubloompilot.models

import java.time.Instant

data class DailyHealthSummary(
    val sleepStartTime: Instant,
    val sleepEndTime: Instant,
    val sleepDurationMinutes: Long,
    val deepSleepMinutes: Long,
    val lightSleepMinutes: Long = 0L,
    val remSleepMinutes: Long = 0L,
    val awakeSleepMinutes: Long = 0L,
    val wakeHeartRate: Long?,
    val averageHrvMillis: Int?,
    val stepsLast24Hours: Long
)