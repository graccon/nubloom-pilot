package com.sujin.nubloompilot.models

enum class RecoveryRhythmMarkerType {
    NAP,
    CAFFEINE,
    LIGHT,
    LIGHT_BLOCK,
    SLEEP_PREPARATION,
    MAIN_SLEEP
}

data class RecoveryRhythmSeriesPoint(
    val hour: Float,
    val level: Float,
    val isWorkTime: Boolean = false
)

data class RecoveryRhythmSeries(
    val shiftType: ShiftInsightType,
    val label: String,
    val points: List<RecoveryRhythmSeriesPoint>,
    val workStartHour: Float? = null,
    val workEndHour: Float? = null
)

data class RecoveryRhythmMarker(
    val type: RecoveryRhythmMarkerType,
    val hour: Float,
    val title: String,
    val description: String
)

data class RecoverySleepWindow(
    val label: String,
    val startHour: Float,
    val endHour: Float,
    val colorType: ShiftInsightType? = null
)

data class RecoveryRhythmGraphData(
    val title: String,
    val description: String,
    val series: List<RecoveryRhythmSeries> = emptyList(),
    val markers: List<RecoveryRhythmMarker> = emptyList(),
    val sleepWindows: List<RecoverySleepWindow> = emptyList(),
    
    // Legacy support
    @Deprecated("Use series")
    val points: List<RecoveryRhythmPoint> = emptyList(),
    @Deprecated("Use series")
    val baselineLabel: String = "나의 평소 리듬",
    @Deprecated("Use series")
    val adjustedLabel: String = "오늘 근무 기준 예상 리듬"
)

data class RecoveryRhythmPoint(
    val hour: Float,
    val baselineLevel: Float,
    val shiftAdjustedLevel: Float
)
