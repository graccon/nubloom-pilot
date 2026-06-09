package com.sujin.nubloompilot.logic

import com.sujin.nubloompilot.models.*
import kotlin.math.cos
import kotlin.math.PI

class RecoveryRhythmGenerator {

    fun generate(
        baselineProfile: MctqBaselineProfile?,
        shiftInsightSummary: ShiftInsightSummary
    ): RecoveryRhythmGraphData {
        val series = listOf(
            generateSeries(ShiftInsightType.DAY, baselineProfile, shiftInsightSummary.dayInsight),
            generateSeries(ShiftInsightType.EVENING, baselineProfile, shiftInsightSummary.eveningInsight),
            generateSeries(ShiftInsightType.NIGHT, baselineProfile, shiftInsightSummary.nightInsight),
            generateSeries(ShiftInsightType.OFF, baselineProfile, shiftInsightSummary.offInsight)
        )

        return RecoveryRhythmGraphData(
            title = "근무 유형별 예상 회복 리듬",
            description = "MCTQ 기반 평소 수면 리듬과 근무별 수면 패턴, 피로도를 바탕으로 예상 회복 흐름을 계산했어요.",
            series = series,
            markers = emptyList()
        )
    }

    private fun generateSeries(
        shiftType: ShiftInsightType,
        baselineProfile: MctqBaselineProfile?,
        typeInsight: ShiftTypeInsight?
    ): RecoveryRhythmSeries {
        val label = when (shiftType) {
            ShiftInsightType.DAY -> "Day 근무"
            ShiftInsightType.EVENING -> "Evening 근무"
            ShiftInsightType.NIGHT -> "Night 근무"
            ShiftInsightType.OFF -> "Off / 쉬는 날"
        }

        val (workStart, workEnd) = getWorkStartEnd(shiftType)
        val pattern = typeInsight?.regularPattern
        
        val midSleepHour = getMidSleepHour(shiftType, baselineProfile)
        val activePeakHour = (midSleepHour + 12f) % 24f

        // Increase density: Every 15 minutes (4 steps per hour)
        val stepsPerHour = 4
        val points = (0..24 * stepsPerHour).map { step ->
            val hour = step.toFloat() / stepsPerHour
            val isWork = isHourInWorkTime(hour, workStart, workEnd)
            
            val circadian = calculateCircadianLevel(hour, activePeakHour)
            
            val sleepDebtPenalty = calculateSleepDebtPenalty(pattern?.averageSleepDurationMinutes)
            val fatiguePenalty = calculateFatiguePenalty(pattern?.averageFatigueLevel)
            val (gloryRiskPenalty, stabilityBonus) = calculateMorningGloryImpact(pattern)
            
            val workBurden = if (isWork) calculateWorkBurdenPenalty(hour, workStart!!, workEnd!!) else 0f
            
            // Smoother Night Penalty (Fade in/out around 00:00 and 06:00)
            val nightPenalty = if (isWork && shiftType == ShiftInsightType.NIGHT) {
                when {
                    hour in 0f..6f -> 0.12f
                    hour > 23f -> (hour - 23f) * 0.12f // Ramp up from 23h to 00h
                    hour > 6f && hour < 7f -> (7f - hour) * 0.12f // Ramp down from 06h to 07h
                    else -> 0f
                }
            } else 0f

            val finalLevel = circadian + stabilityBonus - sleepDebtPenalty - fatiguePenalty - gloryRiskPenalty - workBurden - nightPenalty
            
            RecoveryRhythmSeriesPoint(
                hour = hour,
                level = clampLevel(finalLevel),
                isWorkTime = isWork
            )
        }

        return RecoveryRhythmSeries(
            shiftType = shiftType,
            label = label,
            points = points,
            workStartHour = workStart,
            workEndHour = workEnd
        )
    }

    private fun calculateCircadianLevel(hour: Float, peakHour: Float): Float {
        // 0.5f + 0.35f * cos(2π * (hour - activePeakHour) / 24)
        return 0.5f + 0.35f * cos(2.0 * PI * (hour - peakHour) / 24.0).toFloat()
    }

    private fun calculateSleepDebtPenalty(durationMinutes: Long?): Float {
        if (durationMinutes == null) return 0.05f
        val hours = durationMinutes / 60f
        return when {
            hours >= 7f -> 0f
            hours >= 6f -> 0.05f
            hours >= 4.5f -> 0.12f
            else -> 0.20f
        }
    }

    private fun calculateFatiguePenalty(fatigueLevel: Double?): Float {
        if (fatigueLevel == null) return 0.05f
        return when {
            fatigueLevel < 3.0 -> 0f
            fatigueLevel < 4.0 -> 0.05f
            fatigueLevel < 5.0 -> 0.12f
            else -> 0.18f
        }
    }

    private fun calculateMorningGloryImpact(pattern: ShiftPatternInsight?): Pair<Float, Float> {
        val counts = pattern?.morningGloryTypeCounts ?: return 0.05f to 0f
        val sampleCount = pattern.sampleCount
        if (sampleCount == 0) return 0.05f to 0f

        val riskCount = (counts["TYPE_3"] ?: 0) + (counts["TYPE_4"] ?: 0)
        val riskRatio = riskCount.toFloat() / sampleCount

        val penalty = when {
            riskRatio < 0.25f -> 0f
            riskRatio < 0.50f -> 0.05f
            riskRatio < 0.75f -> 0.10f
            else -> 0.15f
        }

        val type1Count = counts["TYPE_1"] ?: 0
        val type1Ratio = type1Count.toFloat() / sampleCount
        val bonus = if (type1Ratio >= 0.6f) 0.05f else 0f

        return penalty to bonus
    }

    private fun calculateWorkBurdenPenalty(hour: Float, start: Float, end: Float): Float {
        val totalDuration = if (end > start) end - start else (24f - start) + end
        val progress = if (end > start) {
            (hour - start) / totalDuration
        } else {
            val currentFromStart = if (hour >= start) hour - start else (24f - start) + hour
            currentFromStart / totalDuration
        }
        
        // workBurdenPenalty = 0.05f + workProgress * 0.15f
        return 0.05f + progress.coerceIn(0f, 1f) * 0.15f
    }

    private fun isHourInWorkTime(hour: Float, start: Float?, end: Float?): Boolean {
        if (start == null || end == null) return false
        return if (start < end) {
            hour >= start && hour <= end
        } else {
            hour >= start || hour <= end
        }
    }

    private fun getMidSleepHour(shiftType: ShiftInsightType, profile: MctqBaselineProfile?): Float {
        val timeStr = when (shiftType) {
            ShiftInsightType.DAY -> profile?.dayWorkMidSleep
            ShiftInsightType.EVENING -> profile?.eveningWorkMidSleep
            ShiftInsightType.NIGHT -> profile?.nightWorkMidSleep
            ShiftInsightType.OFF -> profile?.dayFreeMidSleep // Using dayFreeMidSleep as proxy for Off
        }

        return timeStr?.let {
            runCatching {
                val parts = it.split(":")
                parts[0].toFloat() + parts[1].toFloat() / 60f
            }.getOrNull()
        } ?: when (shiftType) {
            ShiftInsightType.DAY -> 3.0f
            ShiftInsightType.EVENING -> 4.0f
            ShiftInsightType.NIGHT -> 9.0f
            ShiftInsightType.OFF -> 4.0f
        }
    }

    private fun getWorkStartEnd(shiftType: ShiftInsightType): Pair<Float?, Float?> {
        return when (shiftType) {
            ShiftInsightType.DAY -> 6.5f to 15.5f
            ShiftInsightType.EVENING -> 14.5f to 23.5f
            ShiftInsightType.NIGHT -> 22.5f to 7.5f
            ShiftInsightType.OFF -> null to null
        }
    }

    private fun clampLevel(level: Float): Float {
        return level.coerceIn(0.05f, 0.95f)
    }
}
