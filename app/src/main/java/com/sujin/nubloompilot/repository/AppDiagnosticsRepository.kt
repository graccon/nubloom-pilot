package com.sujin.nubloompilot.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.sujin.nubloompilot.models.AppDiagnosticsState
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AppDiagnosticsRepository(
    private val context: Context,
    private val healthConnectRepository: HealthConnectRepository,
    private val shiftScheduleRepository: ShiftScheduleRepository
) {
    suspend fun getDiagnosticsState(
        baselineProfileExists: Boolean
    ): AppDiagnosticsState {
        val isHealthConnectAvailable = healthConnectRepository.isHealthConnectAvailable()
        val today = LocalDate.now()
        val todayShift = shiftScheduleRepository.getShiftForDate(today)

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
                    // 1. Check Sleep
                    val latestSession = healthConnectRepository.getLatestSleepSession()
                    if (latestSession != null) {
                        val zoneId = ZoneId.systemDefault()
                        val endDateTime = latestSession.endTime.atZone(zoneId)
                        todaySleepSynced = endDateTime.toLocalDate() == today
                        lastSleepSyncTimeText = endDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }

                    // 2. Check Heart Rate & Steps (Last 24 hours)
                    val now = Instant.now()
                    val dayAgo = now.minusSeconds(86400)

                    heartRateAvailable = healthConnectRepository.readHeartRates(dayAgo, now).isNotEmpty()
                    stepsAvailable = healthConnectRepository.readSteps(dayAgo, now).isNotEmpty()
                }
            } catch (e: Exception) {
                Log.e("AppDiagnosticsRepo", "Failed to fetch health data for diagnostics", e)
            }
        }

        return AppDiagnosticsState(
            notificationEnabled = isNotificationGranted,
            healthConnectInstalled = isHealthConnectAvailable,
            healthConnectPermissionGranted = healthPermissionGranted,
            todaySleepSynced = todaySleepSynced,
            lastSleepSyncTimeText = lastSleepSyncTimeText,
            heartRateDataAvailable = heartRateAvailable,
            stepsDataAvailable = stepsAvailable,
            mctqCompleted = baselineProfileExists,
            todayDutyRegistered = !todayShift.isNullOrBlank(),
            checkInNotificationScheduled = SleepCheckInNotificationScheduler.isCheckInNotificationScheduled(context),
            nextCheckInNotificationTimeText = null
        )
    }
}
