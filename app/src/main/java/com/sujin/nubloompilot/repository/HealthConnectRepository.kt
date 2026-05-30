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
import com.sujin.nubloompilot.models.SleepEpisode
import java.time.Duration
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
        return runCatching {
            val grantedPermissions =
                client.permissionController.getGrantedPermissions()
            grantedPermissions.containsAll(healthPermissions)
        }.getOrDefault(false)
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
        return runCatching {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime,
                        endTime
                    )
                )
            )
            response.records
        }.getOrDefault(emptyList())
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

    /**
     * Fetches and merges fragmented sleep sessions into episodes.
     * Sessions with gaps less than [mergeThresholdMinutes] are considered part of the same episode.
     */
    suspend fun getLatestSleepEpisode(
        lookBackHours: Long = 36L,
        mergeThresholdMinutes: Long = 120L
    ): SleepEpisode? {
        val sessions = getRecentSleepSessions(lookBackHours = lookBackHours)
        if (sessions.isEmpty()) return null

        val episodes = mutableListOf<MutableList<SleepSessionRecord>>()
        var currentGroup = mutableListOf<SleepSessionRecord>()

        for (session in sessions) {
            if (currentGroup.isEmpty()) {
                currentGroup.add(session)
            } else {
                val lastSession = currentGroup.last()
                val gap = Duration.between(lastSession.endTime, session.startTime).toMinutes()

                if (gap <= mergeThresholdMinutes) {
                    currentGroup.add(session)
                } else {
                    episodes.add(currentGroup)
                    currentGroup = mutableListOf(session)
                }
            }
        }
        if (currentGroup.isNotEmpty()) episodes.add(currentGroup)

        val latestGroup = episodes.lastOrNull() ?: return null
        
        val startTime = latestGroup.first().startTime
        val endTime = latestGroup.last().endTime
        
        // Duration of sleep fragments only (excluding wake gaps between segments)
        val fragmentDurationMinutes = latestGroup.sumOf { 
            Duration.between(it.startTime, it.endTime).toMinutes() 
        }

        // Total deep sleep across fragments
        val deepSleepMinutes = latestGroup.sumOf { session ->
            session.stages
                .filter { it.stage == SleepSessionRecord.STAGE_TYPE_DEEP }
                .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
        }

        return SleepEpisode(
            startTime = startTime,
            endTime = endTime,
            durationMinutes = fragmentDurationMinutes,
            deepSleepMinutes = deepSleepMinutes
        )
    }

    // TODO 수면 세션 병합 로직 도입
    suspend fun getRecentSleepSessions(
        lookBackHours: Long = 36L
    ): List<SleepSessionRecord> {
        val now = Instant.now()
        val startTime = now.minusSeconds(lookBackHours * 60 * 60)

        return readSleepSessions(
            startTime = startTime,
            endTime = now
        )
            .filter { session ->
                session.endTime <= now
            }
            .sortedBy { session ->
                session.startTime
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
        return runCatching {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime,
                        endTime
                    )
                )
            )
            response.records
        }.getOrDefault(emptyList())
    }

    suspend fun readHrvRecords(
        startTime: Instant,
        endTime: Instant
    ): List<HeartRateVariabilityRmssdRecord> {
        return runCatching {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime,
                        endTime
                    )
                )
            )
            response.records
        }.getOrDefault(emptyList())
    }

    suspend fun readSteps(
        startTime: Instant,
        endTime: Instant
    ): List<StepsRecord> {
        return runCatching {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime,
                        endTime
                    )
                )
            )
            response.records
        }.getOrDefault(emptyList())
    }
}