package com.sujin.nubloompilot.repository

import android.util.Log
import androidx.health.connect.client.records.SleepSessionRecord
import com.sujin.nubloompilot.models.DailyHealthSummary
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

class HealthSummaryRepository(
    private val healthConnectRepository: HealthConnectRepository
) {
    suspend fun getLatestHealthSummary(): DailyHealthSummary? {
        val sleepEpisode =
            healthConnectRepository.getLatestSleepEpisode()
                ?: return null

        val now = Instant.now()

        val wakeHeartRate =
            getWakeHeartRate(
                sleepEndTime = sleepEpisode.endTime
            )

        val stepsLast24Hours =
            getStepsLast24Hours(
                now = now
            )

        return DailyHealthSummary(
            sleepStartTime = sleepEpisode.startTime,
            sleepEndTime = sleepEpisode.endTime,
            sleepDurationMinutes = sleepEpisode.durationMinutes,
            deepSleepMinutes = sleepEpisode.deepSleepMinutes,
            lightSleepMinutes = sleepEpisode.lightSleepMinutes,
            remSleepMinutes = sleepEpisode.remSleepMinutes,
            awakeSleepMinutes = sleepEpisode.awakeSleepMinutes,
            wakeHeartRate = wakeHeartRate,
            averageHrvMillis = null, // HRV merging is more complex, keeping it null for now
            stepsLast24Hours = stepsLast24Hours
        ).also {
            Log.d("HealthSummaryRepo", "Mapped Latest Summary: Deep=${it.deepSleepMinutes}, Light=${it.lightSleepMinutes}, REM=${it.remSleepMinutes}, Awake=${it.awakeSleepMinutes}")
        }
    }

    private suspend fun getWakeHeartRate(
        sleepEndTime: Instant
    ): Long? {
        val heartRateRecords =
            healthConnectRepository.readHeartRates(
                startTime = sleepEndTime.minusSeconds(1800),
                endTime = sleepEndTime.plusSeconds(1800)
            )

        return heartRateRecords
            .minByOrNull { record ->
                abs(
                    Duration.between(
                        sleepEndTime,
                        record.startTime
                    ).seconds
                )
            }
            ?.samples
            ?.firstOrNull()
            ?.beatsPerMinute
    }

    private suspend fun getAverageHrvMillis(
        sleepSession: SleepSessionRecord
    ): Int? {
        val hrvRecords =
            healthConnectRepository.readHrvRecords(
                startTime = sleepSession.startTime,
                endTime = sleepSession.endTime
            )

        return hrvRecords
            .map { record ->
                record.heartRateVariabilityMillis
            }
            .average()
            .takeIf { average ->
                !average.isNaN()
            }
            ?.toInt()
    }



    private suspend fun getStepsLast24Hours(
        now: Instant
    ): Long {
        val stepRecords =
            healthConnectRepository.readSteps(
                startTime = now.minusSeconds(86400),
                endTime = now
            )

        return stepRecords.sumOf { record ->
            record.count
        }
    }
    suspend fun getRecentHealthSummaries(
        limit: Int = 4,
        lookBackDays: Long = 14
    ): List<DailyHealthSummary> {
        val sleepSessions =
            healthConnectRepository.getRecentSleepSessions(
                limit = limit,
                lookBackDays = lookBackDays
            )

        return sleepSessions.map { sleepSession ->
            val now = Instant.now()

            val sleepDurationMinutes =
                Duration.between(
                    sleepSession.startTime,
                    sleepSession.endTime
                ).toMinutes()

            var deepMinutes = 0L
            var lightMinutes = 0L
            var remMinutes = 0L
            var awakeMinutes = 0L

            sleepSession.stages.forEach { stage ->
                val duration = Duration.between(stage.startTime, stage.endTime).toMinutes()
                when (stage.stage) {
                    SleepSessionRecord.STAGE_TYPE_DEEP -> deepMinutes += duration
                    SleepSessionRecord.STAGE_TYPE_LIGHT -> lightMinutes += duration
                    SleepSessionRecord.STAGE_TYPE_REM -> remMinutes += duration
                    SleepSessionRecord.STAGE_TYPE_AWAKE -> awakeMinutes += duration
                }
            }

            val wakeHeartRate =
                getWakeHeartRate(
                    sleepEndTime = sleepSession.endTime
                )

            val averageHrvMillis =
                getAverageHrvMillis(
                    sleepSession = sleepSession
                )

            val stepsLast24Hours =
                getStepsLast24Hours(
                    now = now
                )

            DailyHealthSummary(
                sleepStartTime = sleepSession.startTime,
                sleepEndTime = sleepSession.endTime,
                sleepDurationMinutes = sleepDurationMinutes,
                deepSleepMinutes = deepMinutes,
                lightSleepMinutes = lightMinutes,
                remSleepMinutes = remMinutes,
                awakeSleepMinutes = awakeMinutes,
                wakeHeartRate = wakeHeartRate,
                averageHrvMillis = averageHrvMillis,
                stepsLast24Hours = stepsLast24Hours
            )
        }
    }

    fun getBaselineSleepDurationMinutes(
        recentSummaries: List<DailyHealthSummary>
    ): Long? {
        val baselineTargets = recentSummaries.drop(1)

        if (baselineTargets.size < 3) return null

        return baselineTargets
            .map { it.sleepDurationMinutes }
            .average()
            .toLong()
    }

    fun getBaselineWakeHeartRate(
        recentSummaries: List<DailyHealthSummary>
    ): Long? {
        val baselineTargets = recentSummaries.drop(1)

        val heartRates =
            baselineTargets.mapNotNull { it.wakeHeartRate }

        if (heartRates.size < 3) return null

        return heartRates
            .average()
            .toLong()
    }
}