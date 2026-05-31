package com.sujin.nubloompilot.models

/**
 * Data model representing the diagnostic state of the application.
 */
data class AppDiagnosticsState(
    val notificationEnabled: Boolean = false,
    val healthConnectInstalled: Boolean = false,
    val healthConnectPermissionGranted: Boolean = false,
    val todaySleepSynced: Boolean = false,
    val lastSleepSyncTimeText: String? = null,
    val heartRateDataAvailable: Boolean = false,
    val stepsDataAvailable: Boolean = false,
    val mctqCompleted: Boolean = false,
    val todayDutyRegistered: Boolean = false,
    val checkInNotificationScheduled: Boolean = false,
    val nextCheckInNotificationTimeText: String? = null,
    val activeInterventionExists: Boolean = false
) {
    /**
     * True if all critical components are ready for normal operation.
     */
    val isReady: Boolean
        get() = notificationEnabled &&
                healthConnectInstalled &&
                healthConnectPermissionGranted &&
                todaySleepSynced &&
                mctqCompleted &&
                todayDutyRegistered

    /**
     * The number of items that require user attention.
     */
    val warningCount: Int
        get() {
            var count = 0
            if (!notificationEnabled) count++
            if (!healthConnectInstalled) count++
            if (!healthConnectPermissionGranted) count++
            if (!todaySleepSynced) count++
            if (!heartRateDataAvailable) count++
            if (!stepsDataAvailable) count++
            if (!mctqCompleted) count++
            if (!todayDutyRegistered) count++
            if (!activeInterventionExists) count++
            return count
        }
}
