package com.sujin.nubloompilot.models

enum class ShiftInsightType {
    DAY,
    EVENING,
    NIGHT,
    OFF
}

data class ShiftPatternInsight(
    val sampleCount: Int = 0,
    val averageSleepDurationMinutes: Long? = null,
    val averageFatigueLevel: Double? = null,
    val mostCommonMorningGloryType: String? = null,
    val morningGloryTypeCounts: Map<String, Int> = emptyMap(),
    val featureText: String? = null,
    val hasEnoughData: Boolean = false
)

data class ShiftTypeInsight(
    val shiftType: ShiftInsightType,
    val regularPattern: ShiftPatternInsight? = null,
    val transitionPattern: ShiftPatternInsight? = null
)

data class ShiftInsightSummary(
    val dayInsight: ShiftTypeInsight? = null,
    val eveningInsight: ShiftTypeInsight? = null,
    val nightInsight: ShiftTypeInsight? = null,
    val offInsight: ShiftTypeInsight? = null,
    val recoveryRhythmGraphData: RecoveryRhythmGraphData? = null,
    
    // Legacy fields to maintain compatibility during migration
    @Deprecated("Use per-shift insights")
    val maintenanceInsight: ShiftFlowInsight? = null,
    @Deprecated("Use per-shift insights")
    val returnFromOffInsight: ShiftFlowInsight? = null
)

// Legacy models - Keep for build stability during transition
@Deprecated("Use ShiftInsightType")
enum class ShiftFlowType {
    WORK_MAINTENANCE,
    RETURN_FROM_OFF
}

@Deprecated("Use ShiftPatternInsight")
data class ShiftFlowInsight(
    val title: String,
    val description: String,
    val sampleCount: Int,
    val averageSleepDurationMinutes: Long? = null,
    val averageFatigueLevel: Double? = null,
    val mostCommonMorningGloryType: String? = null,
    val hasEnoughData: Boolean = false
)
