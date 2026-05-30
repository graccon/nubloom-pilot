package com.sujin.nubloompilot.repository

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
            sleepEndTime = sleepEpisode.endTime,
            sleepDurationMinutes = sleepEpisode.durationMinutes,
            deepSleepMinutes = sleepEpisode.deepSleepMinutes,
            wakeHeartRate = wakeHeartRate,
            averageHrvMillis = null, // HRV merging is more complex, keeping it null for now
            stepsLast24Hours = stepsLast24Hours
        )
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

            val deepSleepMinutes =
                sleepSession.stages
                    .filter { stage ->
                        stage.stage == SleepSessionRecord.STAGE_TYPE_DEEP
                    }
                    .sumOf { stage ->
                        Duration.between(
                            stage.startTime,
                            stage.endTime
                        ).toMinutes()
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
                sleepEndTime = sleepSession.endTime,
                sleepDurationMinutes = sleepDurationMinutes,
                deepSleepMinutes = deepSleepMinutes,
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