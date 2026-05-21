package com.sujin.nubloompilot.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectRepository(
    private val context: Context
) {
    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val healthPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) ==
                HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasHealthPermissions(): Boolean {
        val grantedPermissions =
            client.permissionController.getGrantedPermissions()

        return grantedPermissions.containsAll(healthPermissions)
    }

    suspend fun readSleepSessions(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<SleepSessionRecord> {
        val zoneId = ZoneId.systemDefault()

        return readSleepSessions(
            startTime = startDate.atStartOfDay(zoneId).toInstant(),
            endTime = endDate.atStartOfDay(zoneId).toInstant()
        )
    }

    suspend fun readSleepSessions(
        startTime: Instant,
        endTime: Instant
    ): List<SleepSessionRecord> {
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

    suspend fun getLatestSleepSession(
        lookBackDays: Long = 3
    ): SleepSessionRecord? {
        val today = LocalDate.now()
        val now = Instant.now()

        return readSleepSessions(
            startDate = today.minusDays(lookBackDays),
            endDate = today.plusDays(1)
        )
            .filter { session ->
                session.endTime <= now
            }
            .maxByOrNull { session ->
                session.endTime
            }
    }

    suspend fun getRecentSleepSessions(
        limit: Int = 3,
        lookBackDays: Long = 7
    ): List<SleepSessionRecord> {
        val today = LocalDate.now()
        val now = Instant.now()

        return readSleepSessions(
            startDate = today.minusDays(lookBackDays),
            endDate = today.plusDays(1)
        )
            .filter { session ->
                session.endTime <= now
            }
            .sortedByDescending { session ->
                session.endTime
            }
            .take(limit)
    }

    suspend fun readHeartRates(
        startTime: Instant,
        endTime: Instant
    ): List<HeartRateRecord> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    startTime,
                    endTime
                )
            )
        )

        return response.records
    }

    suspend fun readHrvRecords(
        startTime: Instant,
        endTime: Instant
    ): List<HeartRateVariabilityRmssdRecord> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    startTime,
                    endTime
                )
            )
        )

        return response.records
    }

    suspend fun readSteps(
        startTime: Instant,
        endTime: Instant
    ): List<StepsRecord> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    startTime,
                    endTime
                )
            )
        )

        return response.records
    }
}