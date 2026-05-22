package com.sujin.nubloompilot.models

enum class SignalState {
    GOOD,
    NEUTRAL,
    CAUTION,
    UNKNOWN
}

data class SleepInterpretationInput(
    val sleepDurationMinutes: Long?,
    val wakeHeartRate: Long?,
    val baselineSleepDurationMinutes: Long?,
    val baselineWakeHeartRate: Long?
)

data class InterpretationBlock(
    val state: SignalState,
    val title: String,
    val description: String
)

data class SleepInterpretationResult(
    val sleepDuration: InterpretationBlock,
    val recovery: InterpretationBlock
)