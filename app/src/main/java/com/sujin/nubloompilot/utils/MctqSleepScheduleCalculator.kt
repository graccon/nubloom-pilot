package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import java.time.LocalDateTime

object MctqSleepScheduleCalculator {

    fun calculate(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile
    ): SleepScheduleResult {
        val baselineSleepDurationMinutes = getBaselineSleepDurationMinutes(
            context = context,
            baseline = baseline
        )

        val targetSleepDurationMinutes = calculateTargetSleepDurationMinutes(
            context = context,
            baselineSleepDurationMinutes = baselineSleepDurationMinutes,
            behavior = behavior
        )

        val targetSleepStart = calculateTargetSleepStart(
            context = context,
            baseline = baseline,
            targetSleepDurationMinutes = targetSleepDurationMinutes
        )

        val targetSleepEnd = targetSleepStart.plusMinutes(
            targetSleepDurationMinutes.toLong()
        )

        val constrainedSchedule = applyNextShiftConstraint(
            context = context,
            targetSleepStart = targetSleepStart,
            targetSleepEnd = targetSleepEnd,
            targetSleepDurationMinutes = targetSleepDurationMinutes
        )

        val adjustmentReason = createAdjustmentReason(
            context = context,
            behavior = behavior,
            constraintReason = constrainedSchedule.constraintReason
        )

        return SleepScheduleResult(
            targetSleepStart = constrainedSchedule.targetSleepStart,
            targetSleepEnd = constrainedSchedule.targetSleepEnd,
            targetSleepDurationMinutes = constrainedSchedule.targetSleepDurationMinutes,
            baselineSleepDurationMinutes = baselineSleepDurationMinutes,
            adjustmentReason = adjustmentReason
        )
    }

    private fun getBaselineSleepDurationMinutes(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile
    ): Int {
        return when (context.currentShift) {
            ShiftType.DAY -> baseline.dayWorkSleepDurationMinutes
            ShiftType.EVENING -> baseline.eveningWorkSleepDurationMinutes
            ShiftType.NIGHT -> baseline.nightWorkSleepDurationMinutes
            ShiftType.OFF -> baseline.averageFreeSleepDurationMinutes
        }
    }

    private fun calculateTargetSleepDurationMinutes(
        context: SleepInterventionContext,
        baselineSleepDurationMinutes: Int,
        behavior: MctqBehaviorProfile
    ): Int {
        var totalAdjustmentMinutes = 0

        if (context.objectiveRecoveryLevel <= 2) {
            totalAdjustmentMinutes += 60
        }

        if (context.subjectiveFatigueLevel >= 4) {
            totalAdjustmentMinutes += 45
        }

        if (behavior.vulnerableShift == context.currentShift) {
            totalAdjustmentMinutes += 30
        }

        return (baselineSleepDurationMinutes + totalAdjustmentMinutes)
            .coerceIn(
                minimumValue = 300,
                maximumValue = 540
            )
    }

    private fun calculateTargetSleepStart(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        targetSleepDurationMinutes: Int
    ): LocalDateTime {
        val ruleTarget = context.targetSleepTime

        val baselineMidSleep = getBaselineMidSleepTime(
            context = context,
            baseline = baseline
        ) ?: return ruleTarget

        val baselineTarget = estimateSleepStartFromMidSleep(
            ruleTarget = ruleTarget,
            midSleepTime = baselineMidSleep,
            targetSleepDurationMinutes = targetSleepDurationMinutes
        )

        val diffMinutes = kotlin.math.abs(
            java.time.Duration.between(ruleTarget, baselineTarget).toMinutes()
        )

        return when {
            diffMinutes <= 120 -> {
                blendSleepStart(
                    ruleTarget = ruleTarget,
                    baselineTarget = baselineTarget,
                    baselineWeight = 0.5
                )
            }

            else -> ruleTarget
        }
    }

    private fun getBaselineMidSleepTime(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile
    ): String? {
        return when (context.currentShift) {
            ShiftType.DAY -> baseline.dayWorkMidSleep
            ShiftType.EVENING -> baseline.eveningWorkMidSleep
            ShiftType.NIGHT -> baseline.nightWorkMidSleep
            ShiftType.OFF -> baseline.chronotypeMsfEsc
        }
    }

    private fun estimateSleepStartFromMidSleep(
        ruleTarget: LocalDateTime,
        midSleepTime: String,
        targetSleepDurationMinutes: Int
    ): LocalDateTime {
        val midSleepLocalTime = parseLocalTime(midSleepTime) ?: return ruleTarget

        var baselineMidSleepDateTime = LocalDateTime.of(
            ruleTarget.toLocalDate(),
            midSleepLocalTime
        )

        val halfDurationMinutes = targetSleepDurationMinutes / 2L
        var baselineSleepStart = baselineMidSleepDateTime.minusMinutes(halfDurationMinutes)

        while (baselineSleepStart.isAfter(ruleTarget.plusHours(12))) {
            baselineSleepStart = baselineSleepStart.minusDays(1)
        }

        while (baselineSleepStart.isBefore(ruleTarget.minusHours(12))) {
            baselineSleepStart = baselineSleepStart.plusDays(1)
        }

        return baselineSleepStart
    }

    private fun parseLocalTime(time: String): java.time.LocalTime? {
        return runCatching {
            java.time.LocalTime.parse(time)
        }.getOrNull()
    }

    private fun applyNextShiftConstraint(
        context: SleepInterventionContext,
        targetSleepStart: LocalDateTime,
        targetSleepEnd: LocalDateTime,
        targetSleepDurationMinutes: Int
    ): ConstrainedSleepSchedule {
        val nextShift = context.nextShift ?: return ConstrainedSleepSchedule(
            targetSleepStart = targetSleepStart,
            targetSleepEnd = targetSleepEnd,
            targetSleepDurationMinutes = targetSleepDurationMinutes
        )

        if (nextShift == ShiftType.OFF) {
            return ConstrainedSleepSchedule(
                targetSleepStart = targetSleepStart,
                targetSleepEnd = targetSleepEnd,
                targetSleepDurationMinutes = targetSleepDurationMinutes
            )
        }

        val nextShiftStart = nextShift
            .getTimeRange(context.workDate.plusDays(1))
            .startTime
            ?: return ConstrainedSleepSchedule(
                targetSleepStart = targetSleepStart,
                targetSleepEnd = targetSleepEnd,
                targetSleepDurationMinutes = targetSleepDurationMinutes
            )

        val commuteMinutes = 60L
        val preWorkPreparationMinutes = 60L

        val latestSafeSleepEnd = nextShiftStart
            .minusMinutes(commuteMinutes)
            .minusMinutes(preWorkPreparationMinutes)

        if (!targetSleepEnd.isAfter(latestSafeSleepEnd)) {
            return ConstrainedSleepSchedule(
                targetSleepStart = targetSleepStart,
                targetSleepEnd = targetSleepEnd,
                targetSleepDurationMinutes = targetSleepDurationMinutes
            )
        }

        val constrainedEnd = latestSafeSleepEnd

        val constrainedDurationMinutes = java.time.Duration.between(
            targetSleepStart,
            constrainedEnd
        ).toMinutes().toInt()

        if (constrainedDurationMinutes <= 0) {
            return ConstrainedSleepSchedule(
                targetSleepStart = targetSleepStart,
                targetSleepEnd = targetSleepStart,
                targetSleepDurationMinutes = 0,
                constraintReason = "다음 근무까지 남은 시간이 부족해 수면 시간을 확보하기 어려운 상태예요."
            )
        }

        return ConstrainedSleepSchedule(
            targetSleepStart = targetSleepStart,
            targetSleepEnd = constrainedEnd,
            targetSleepDurationMinutes = constrainedDurationMinutes,
            constraintReason = "다음 근무 준비 시간을 고려해 수면 종료 시간을 앞당겼어요."
        )
    }

    private data class ConstrainedSleepSchedule(
        val targetSleepStart: LocalDateTime,
        val targetSleepEnd: LocalDateTime,
        val targetSleepDurationMinutes: Int,
        val constraintReason: String? = null
    )

    private fun blendSleepStart(
        ruleTarget: LocalDateTime,
        baselineTarget: LocalDateTime,
        baselineWeight: Double
    ): LocalDateTime {
        val diffMinutes = java.time.Duration.between(
            ruleTarget,
            baselineTarget
        ).toMinutes()

        val adjustedMinutes = (diffMinutes * baselineWeight).toLong()

        return ruleTarget.plusMinutes(adjustedMinutes)
    }

    private fun createAdjustmentReason(
        context: SleepInterventionContext,
        behavior: MctqBehaviorProfile,
        constraintReason: String?
    ): String {
        val baseReason = when {
            context.objectiveRecoveryLevel <= 2 ->
                "오늘 회복 상태가 좋지 않아 수면 시간을 조금 더 확보하도록 조정했어요."

            context.subjectiveFatigueLevel >= 4 ->
                "오늘 피로도가 높은 편이라 평소보다 충분히 잘 수 있도록 조정했어요."

            behavior.vulnerableShift == context.currentShift ->
                "평소 이 근무에서는 회복이 어려운 편이라 수면 시간을 더 확보했어요."

            else ->
                "평소 수면 습관을 바탕으로 오늘의 목표 수면 시간을 설정했어요."
        }

        return if (constraintReason != null) {
            "$baseReason $constraintReason"
        } else {
            baseReason
        }
    }
}