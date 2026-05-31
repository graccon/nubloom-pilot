package com.sujin.nubloompilot.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.sujin.nubloompilot.models.AppDiagnosticsState
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationScheduler
import com.sujin.nubloompilot.local.SleepInterventionLocalStore
import com.sujin.nubloompilot.utils.hasActiveIntervention
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AppDiagnosticsRepository(
    private val context: Context,
    private val healthConnectRepository: HealthConnectRepository,
    private val shiftScheduleRepository: ShiftScheduleRepository,
    private val interventionLocalStore: SleepInterventionLocalStore
) {
    suspend fun getDiagnosticsState(
        baselineProfileExists: Boolean
    ): AppDiagnosticsState {
        val isHealthConnectAvailable = healthConnectRepository.isHealthConnectAvailable()
        val today = LocalDate.now()
        val todayShift = shiftScheduleRepository.getShiftForDate(today)
        val yesterdayShift = shiftScheduleRepository.getShiftForDate(today.minusDays(1))

        val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        var healthPermissionGranted = false
        var todaySleepSynced = false
        var lastSleepSyncTimeText: String? = null
        var heartRateAvailable = false
        var stepsAvailable = false

        if (isHealthConnectAvailable) {
            try {
                healthPermissionGranted = healthConnectRepository.hasHealthPermissions()
                
                if (healthPermissionGranted) {
                    val now = Instant.now()
                    
                    // 1. Check Sleep (within last 30 hours)
                    val latestSession = healthConnectRepository.getLatestSleepSession()
                    if (latestSession != null) {
                        val zoneId = ZoneId.systemDefault()
                        val endDateTime = latestSession.endTime.atZone(zoneId)
                        
                        val sleepFreshThreshold = now.minusSeconds(30 * 60 * 60)
                        todaySleepSynced = latestSession.endTime.isAfter(sleepFreshThreshold)
                        lastSleepSyncTimeText = endDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }

                    // 2. Check Heart Rate & Steps (Last 24 hours)
                    val dayAgo = now.minusSeconds(86400)

                    heartRateAvailable = healthConnectRepository.readHeartRates(dayAgo, now).isNotEmpty()
                    stepsAvailable = healthConnectRepository.readSteps(dayAgo, now).isNotEmpty()
                }
            } catch (e: Exception) {
                Log.e("AppDiagnosticsRepo", "Failed to fetch health data for diagnostics", e)
            }
        }

        val activeIntervention = interventionLocalStore.getLatest().hasActiveIntervention()

        return AppDiagnosticsState(
            notificationEnabled = isNotificationGranted,
            healthConnectInstalled = isHealthConnectAvailable,
            healthConnectPermissionGranted = healthPermissionGranted,
            todaySleepSynced = todaySleepSynced,
            lastSleepSyncTimeText = lastSleepSyncTimeText,
            heartRateDataAvailable = heartRateAvailable,
            stepsDataAvailable = stepsAvailable,
            mctqCompleted = baselineProfileExists,
            todayDutyRegistered = !todayShift.isNullOrBlank() || yesterdayShift == "N",
            checkInNotificationScheduled = SleepCheckInNotificationScheduler.isCheckInNotificationScheduled(context),
            nextCheckInNotificationTimeText = null,
            activeInterventionExists = activeIntervention
        )
    }
}
