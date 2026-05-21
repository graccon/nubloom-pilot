package com.sujin.nubloompilot.models

data class DailyHealthSummary(
    val sleepDurationMinutes: Long,
    val deepSleepMinutes: Long,
    val wakeHeartRate: Long?,
    val averageHrvMillis: Int?,
    val stepsLast24Hours: Long
)