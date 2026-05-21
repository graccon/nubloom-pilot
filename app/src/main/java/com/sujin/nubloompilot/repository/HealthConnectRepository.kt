package com.sujin.nubloompilot.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectRepository(
    private val context: Context
) {
    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val sleepPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) ==
                HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasSleepPermission(): Boolean {
        val grantedPermissions =
            client.permissionController.getGrantedPermissions()

        return grantedPermissions.containsAll(sleepPermissions)
    }

    suspend fun readSleepSessions(
        date: LocalDate
    ): List<SleepSessionRecord> {
        val zoneId = ZoneId.systemDefault()

        val startTime = date
            .atStartOfDay(zoneId)
            .toInstant()

        val endTime = date
            .plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()

        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    startTime,
                    endTime
                )
            )
        )

        return response.records
    }

    suspend fun readSleepSessions(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SleepSessionRecord> {
        val zoneId = ZoneId.systemDefault()

        val startTime = startDate
            .atStartOfDay(zoneId)
            .toInstant()

        val endTime = endDate
            .atStartOfDay(zoneId)
            .toInstant()

        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    startTime,
                    endTime
                )
            )
        )

        return response.records
    }
}