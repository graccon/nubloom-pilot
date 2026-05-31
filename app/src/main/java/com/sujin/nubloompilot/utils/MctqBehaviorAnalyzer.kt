package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.MCTQShiftResponse
import com.sujin.nubloompilot.models.MctqBaselineProfile
import com.sujin.nubloompilot.models.MctqBehaviorProfile
import com.sujin.nubloompilot.shared.models.ShiftType

object MctqBehaviorAnalyzer {

    private const val LATENCY_AVERAGE_RISK_MINUTES = 20
    private const val LATENCY_SINGLE_RISK_MINUTES = 30

    private const val BED_GAP_AVERAGE_RISK_MINUTES = 30
    private const val BED_GAP_SINGLE_RISK_MINUTES = 45

    private const val NAP_HABIT_COUNT_THRESHOLD = 3
    private const val LATE_NAP_WINDOW_MINUTES = 240

    private const val RECOMMENDED_SLEEP_MINUTES = 480

    fun analyze(
        responses: List<MCTQShiftResponse>,
        baseline: MctqBaselineProfile
    ): MctqBehaviorProfile {
        val avgLatency = responses
            .map { it.sleepLatencyMinutes }
            .average()
            .toInt()

        val bedGaps = responses.map { response ->
            calculateBedGapMinutes(response)
        }

        val avgBedGap = bedGaps
            .average()
            .toInt()

        val hasSleepLatencyRisk =
            avgLatency >= LATENCY_AVERAGE_RISK_MINUTES ||
                    responses.any { it.sleepLatencyMinutes >= LATENCY_SINGLE_RISK_MINUTES }

        val hasBedInefficiency =
            avgBedGap >= BED_GAP_AVERAGE_RISK_MINUTES ||
                    bedGaps.any { it >= BED_GAP_SINGLE_RISK_MINUTES }

        val napCount = responses.count { it.napTaken }

        val hasLateNapRisk = responses.any { response ->
            isLateNapRisk(response)
        }

        val vulnerableShift = calculateVulnerableShift(baseline)

        return MctqBehaviorProfile(
            hasSleepLatencyRisk = hasSleepLatencyRisk,
            hasBedInefficiency = hasBedInefficiency,
            hasNapHabit = napCount >= NAP_HABIT_COUNT_THRESHOLD,
            hasLateNapRisk = hasLateNapRisk,
            vulnerableShift = vulnerableShift,
            averageSleepLatencyMinutes = avgLatency,
            averageBedGapMinutes = avgBedGap
        )
    }

    private fun calculateBedGapMinutes(
        response: MCTQShiftResponse
    ): Int {
        val bed = timeToMinutes(response.bedTime)
        val trySleep = timeToMinutes(response.tryToSleepTime)

        return (trySleep - bed + 1440) % 1440
    }

    private fun isLateNapRisk(
        response: MCTQShiftResponse
    ): Boolean {
        if (!response.napTaken) return false

        val napEndTime = response.napEndTime ?: return false

        val napEnd = timeToMinutes(napEndTime)
        val sleepOnset = (
                timeToMinutes(response.tryToSleepTime) +
                        response.sleepLatencyMinutes
                ) % 1440

        val minutesUntilSleep = (sleepOnset - napEnd + 1440) % 1440

        return minutesUntilSleep in 0..LATE_NAP_WINDOW_MINUTES
    }

    private fun calculateVulnerableShift(
        baseline: MctqBaselineProfile
    ): ShiftType {
        val dayScore =
            calculateSleepShortageScore(baseline.dayWorkSleepDurationMinutes) +
                    baseline.socialJetlagDayMinutes

        val eveningScore =
            calculateSleepShortageScore(baseline.eveningWorkSleepDurationMinutes) +
                    baseline.socialJetlagEveningMinutes

        val nightScore =
            calculateSleepShortageScore(baseline.nightWorkSleepDurationMinutes) +
                    baseline.socialJetlagNightMinutes

        return listOf(
            ShiftType.DAY to dayScore,
            ShiftType.EVENING to eveningScore,
            ShiftType.NIGHT to nightScore
        ).maxBy { it.second }.first
    }

    private fun calculateSleepShortageScore(
        sleepDurationMinutes: Int
    ): Int {
        return (RECOMMENDED_SLEEP_MINUTES - sleepDurationMinutes)
            .coerceAtLeast(0)
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0

        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0

        return hour * 60 + minute
    }
}