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
        val sleepSession =
            healthConnectRepository.getLatestSleepSession()
                ?: return null

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

        return DailyHealthSummary(
            sleepDurationMinutes = sleepDurationMinutes,
            deepSleepMinutes = deepSleepMinutes,
            wakeHeartRate = wakeHeartRate,
            averageHrvMillis = averageHrvMillis,
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
}