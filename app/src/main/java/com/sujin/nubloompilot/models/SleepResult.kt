package com.sujin.nubloompilot.models

import java.time.Instant

data class SleepResult(
    val participantId: String,
    val participantName: String,
    val sleepEndTime: Instant,
    val sleepDurationMinutes: Long,
    val wakeHeartRate: Long?,
    val fatigueLevel: Int,
    val morningGloryType: MorningGloryType,
    val timestamp: Long = System.currentTimeMillis(),
    val sleepSummary: DailyHealthSummary? = null
)
