package com.sujin.nubloompilot.models

/**
 * Computed results for a single MCTQ block.
 */
data class MctqBlockComputedResult(
    val shiftType: ShiftType,
    val isWorkday: Boolean,
    val sleepOnsetTime: String,      // "HH:mm"
    val sleepEndTime: String,        // "HH:mm"
    val sleepDurationMinutes: Int,
    val midSleepTime: String,        // "HH:mm"
    val timeInBedMinutes: Int,
    val napDurationMinutes: Int,
    val totalSleepDurationWithNapMinutes: Int,
    val alarmUsed: Boolean,
    val canChooseSleepFreely: Boolean,
    val isValid: Boolean = true,
    val warningReason: String? = null
)

/**
 * Integrated baseline profile based on 6 MCTQ blocks.
 */
data class MctqBaselineProfile(
    val blockResults: List<MctqBlockComputedResult>,
    
    // Durations
    val dayWorkSleepDurationMinutes: Int,
    val dayFreeSleepDurationMinutes: Int,
    val eveningWorkSleepDurationMinutes: Int,
    val eveningFreeSleepDurationMinutes: Int,
    val nightWorkSleepDurationMinutes: Int,
    val nightFreeSleepDurationMinutes: Int,
    
    // Mid-sleep points
    val dayWorkMidSleep: String,
    val dayFreeMidSleep: String,
    val eveningWorkMidSleep: String,
    val eveningFreeMidSleep: String,
    val nightWorkMidSleep: String,
    val nightFreeMidSleep: String,
    
    // Global Averages
    val averageWorkSleepDurationMinutes: Int,
    val averageFreeSleepDurationMinutes: Int,
    /**
     * Unweighted average of all 6 blocks. 
     * Temporary measure until actual schedule frequency is available.
     */
    val unweightedAverageSleepDurationMinutes: Int,
    
    // Gaps and Jetlag
    val workFreeSleepGapMinutes: Int,
    val socialJetlagDayMinutes: Int,
    val socialJetlagEveningMinutes: Int,
    val socialJetlagNightMinutes: Int,
    
    // Chronotype
    val chronotypeMsfEsc: String,    // "HH:mm"
    val chronotypeMsfEscMinutes: Int,
    @Deprecated("Labeling logic is tentative and should be handled by UI/Classifier")
    val chronotypeLabel: String? = null
)
