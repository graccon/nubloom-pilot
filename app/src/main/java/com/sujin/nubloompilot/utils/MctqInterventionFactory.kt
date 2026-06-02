package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import com.sujin.nubloompilot.shared.models.Chronotype
import com.sujin.nubloompilot.shared.models.ShiftType
import com.sujin.nubloompilot.shared.models.ShiftTimeRange
import com.sujin.nubloompilot.shared.models.getTimeRange

object MctqInterventionFactory {

    fun create(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile
    ): List<SleepIntervention> {
        val shiftRange = context.currentShift.getTimeRange(context.workDate, context.shiftTimingConfig)

        val candidates = buildList {
            addAll(createMctqMainSleepIntervention(context, baseline, behavior))
            addAll(createMctqSleepPreparationIntervention(context, baseline, behavior))
            addAll(createMctqNapIntervention(context, baseline, behavior, shiftRange))
            addAll(createMctqCaffeineInterventions(context, baseline, behavior, shiftRange))
            addAll(createMctqLightInterventions(context, baseline, behavior, shiftRange))
        }
            .filter { it.startTime.isBefore(it.endTime) }
            .distinctBy { it.type to it.actionType to it.startTime to it.endTime }

        // Safety: If no MCTQ specific candidates, fallback to standard logic
        if (candidates.isEmpty()) {
            return FallbackInterventionFactory.create(context)
        }

        return candidates
            .sortedByDescending {
                getMctqPriorityScore(
                    intervention = it,
                    context = context,
                    baseline = baseline,
                    behavior = behavior
                )
            }
            .take(4)
            .sortedBy { it.startTime }
    }

    private fun createMctqMainSleepIntervention(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile
    ): List<SleepIntervention> {
        val schedule = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = baseline,
            behavior = behavior
        )

        return listOf(
            SleepIntervention(
                type = InterventionType.MAIN_SLEEP,
                startTime = schedule.targetSleepStart,
                endTime = schedule.targetSleepEnd,
                title = "오늘 목표 수면",
                description = when {
                    context.objectiveRecoveryLevel <= 2 ->
                        "오늘은 회복이 부족한 상태예요. 평소 수면 시간보다 조금 더 길게 자는 것을 목표로 해보세요."

                    context.subjectiveFatigueLevel >= 4 ->
                        "피로감이 높은 날이에요. 오늘은 평소보다 여유 있는 수면 시간을 확보하는 것이 좋아요."

                    behavior.vulnerableShift == context.currentShift ->
                        "이 근무 유형에서는 평소 수면이 짧아지는 경향이 있어요. 오늘은 수면 시간을 우선적으로 확보해보세요."

                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 후에는 수면 시간이 짧아지기 쉬워요. 회복 수면을 안정적으로 확보해보세요."

                    else ->
                        "평소 수면 리듬을 바탕으로 오늘의 목표 수면 시간을 설정했어요."
                },
                reason = schedule.adjustmentReason,
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun createMctqSleepPreparationIntervention(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile
    ): List<SleepIntervention> {
        val sleepStart = context.targetSleepTime

        val preparationMinutes = when {
            behavior.hasSleepLatencyRisk &&
                    (context.subjectiveFatigueLevel >= 4 || context.objectiveRecoveryLevel <= 2) -> 60L

            behavior.hasSleepLatencyRisk ||
                    behavior.hasBedInefficiency ||
                    behavior.hasLateNapRisk -> 45L

            else -> 30L
        }

        val preparationStart = sleepStart.minusMinutes(preparationMinutes)

        return listOf(
            SleepIntervention(
                type = InterventionType.SLEEP_PREPARATION,
                startTime = preparationStart,
                endTime = sleepStart,
                title = "수면 준비 시간",
                description = when {
                    behavior.hasSleepLatencyRisk ->
                        "평소 잠드는 데 시간이 오래 걸리는 편이에요. 목표 수면 전에 조명과 알림, 주변 자극을 미리 줄여보세요."

                    behavior.hasBedInefficiency ->
                        "침대에 누운 뒤 바로 잠들기 어려운 패턴이 보여요. 잠들 준비가 된 뒤 침대에 들어갈 수 있도록 환경을 먼저 정리해보세요."

                    behavior.hasLateNapRisk ->
                        "늦은 낮잠이 밤 수면에 영향을 줄 수 있어요. 목표 수면 전에는 각성을 높이는 활동을 줄여보세요."

                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 후에는 아침 빛과 소음을 줄이는 것이 중요해요. 암막, 눈가리개, 알림 차단을 준비해보세요."

                    else ->
                        "목표 수면 시간에 맞춰 조명과 알림, 주변 자극을 줄이고 쉴 준비를 해보세요."
                },
                reason = when {
                    behavior.hasSleepLatencyRisk ->
                        "평균 수면 진입 시간이 ${behavior.averageSleepLatencyMinutes}분으로 나타나, 수면 전 자극을 줄이는 개입을 우선 배치했어요."

                    behavior.hasBedInefficiency ->
                        "침대에 들어간 뒤 실제 잠들기까지 평균 ${behavior.averageBedGapMinutes}분의 간격이 있어, 수면 준비 시간을 따로 확보했어요."

                    behavior.hasLateNapRisk ->
                        "늦은 낮잠 패턴이 있어 목표 수면 전 각성 수준을 낮추는 것이 중요해요."

                    else ->
                        "기초 수면 설문에서 확인된 평소 수면 리듬을 바탕으로 목표 수면 전 준비 시간을 배치했어요."
                },
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun createMctqNapIntervention(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workStart = shiftRange.startTime

        val needsNap =
            context.currentShift == ShiftType.NIGHT ||
                    context.subjectiveFatigueLevel >= 4 ||
                    context.objectiveRecoveryLevel <= 2 ||
                    behavior.vulnerableShift == context.currentShift ||
                    behavior.hasNapHabit

        if (!needsNap) return emptyList()

        val napStart = when (context.currentShift) {
            ShiftType.NIGHT -> workStart?.minusHours(4)
            ShiftType.EVENING -> workStart?.minusHours(3)
            ShiftType.DAY -> {
                if (
                    context.subjectiveFatigueLevel >= 4 ||
                    context.objectiveRecoveryLevel <= 2 ||
                    behavior.vulnerableShift == ShiftType.DAY
                ) {
                    workStart?.minusHours(2)
                } else {
                    null
                }
            }
            ShiftType.OFF -> context.wakeTime.plusHours(4)
        } ?: return emptyList()

        val napEnd = when (context.currentShift) {
            ShiftType.NIGHT -> workStart?.minusHours(1)
            ShiftType.EVENING -> workStart?.minusHours(1)
            ShiftType.DAY -> workStart?.minusHours(1)
            ShiftType.OFF -> napStart.plusHours(2)
        } ?: return emptyList()

        if (!napStart.isBefore(napEnd)) return emptyList()

        val recommendedNapMinutes = when {
            context.currentShift == ShiftType.NIGHT -> 30
            context.subjectiveFatigueLevel >= 4 -> 30
            context.objectiveRecoveryLevel <= 2 -> 30
            else -> 20
        }

        return listOf(
            SleepIntervention(
                type = InterventionType.NAP,
                startTime = napStart,
                endTime = napEnd,
                title = "짧은 회복 낮잠",
                description = when {
                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 전에는 긴 수면이 어렵더라도, 가능하다면 짧게 눈을 붙여 피로를 덜어보세요."

                    behavior.vulnerableShift == context.currentShift ->
                        "평소 이 근무에서는 수면이 부족해지는 편이에요. 근무 전 짧은 휴식 시간을 확보해보세요."

                    context.subjectiveFatigueLevel >= 4 ->
                        "오늘 피로감이 높은 편이에요. 이 시간대 안에서 가능하다면 ${recommendedNapMinutes}분 정도 짧게 쉬어보세요."

                    context.objectiveRecoveryLevel <= 2 ->
                        "수면 회복이 충분하지 않은 상태예요. 무리하기보다 짧게 회복할 시간을 만들어보세요."

                    behavior.hasNapHabit ->
                        "평소 낮잠으로 피로를 보완하는 패턴이 있어요. 오늘도 필요하다면 짧게 활용해보세요."

                    else ->
                        "오늘 일정 중 가능하다면 ${recommendedNapMinutes}분 정도 짧게 쉬어보세요."
                },
                reason = when {
                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 전 짧은 낮잠은 근무 중 피로 누적을 줄이는 데 도움이 될 수 있어요."

                    behavior.vulnerableShift == context.currentShift ->
                        "평소 기록에서 이 근무 유형은 회복이 어려운 편으로 나타났어요."

                    context.subjectiveFatigueLevel >= 4 ->
                        "오늘 스스로 느끼는 피로감이 높아, 짧은 회복 시간을 우선 배치했어요."

                    context.objectiveRecoveryLevel <= 2 ->
                        "최근 수면 회복 상태가 낮아, 짧은 휴식을 통해 부담을 줄이는 방향으로 설정했어요."

                    behavior.hasNapHabit ->
                        "평소 낮잠을 활용하는 습관을 고려해, 무리하지 않는 짧은 낮잠 시간을 제안했어요."

                    else ->
                        "오늘의 근무 일정과 평소 수면 패턴을 함께 고려해 낮잠 시간을 배치했어요."
                },
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun createMctqCaffeineInterventions(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        return buildList {
            addAll(
                createMctqCaffeineDoIntervention(
                    context = context,
                    behavior = behavior,
                    shiftRange = shiftRange
                )
            )

            addAll(
                createMctqCaffeineAvoidIntervention(
                    context = context,
                    behavior = behavior
                )
            )
        }
    }

    private fun createMctqCaffeineDoIntervention(
        context: SleepInterventionContext,
        behavior: MctqBehaviorProfile,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workStart = shiftRange.startTime ?: return emptyList()
        val workEnd = shiftRange.endTime ?: return emptyList()

        if (context.currentShift == ShiftType.OFF) return emptyList()

        val shouldUseCaffeine =
            context.subjectiveFatigueLevel >= 4 ||
                    context.objectiveRecoveryLevel <= 2 ||
                    behavior.vulnerableShift == context.currentShift ||
                    context.currentShift == ShiftType.NIGHT

        if (!shouldUseCaffeine) return emptyList()

        val useEnd = minOf(
            workStart.plusHours(3),
            workEnd,
            getMctqCaffeineCutoffTime(context, behavior)
        )

        if (!workStart.isBefore(useEnd)) return emptyList()

        return listOf(
            SleepIntervention(
                type = InterventionType.CAFFEINE,
                startTime = workStart,
                endTime = useEnd,
                title = "카페인 활용 가능",
                description = when {
                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무에서는 근무 초반에만 카페인을 가볍게 활용해보세요. 후반에는 다음 수면에 영향을 줄 수 있어요."

                    behavior.vulnerableShift == context.currentShift ->
                        "평소 이 근무에서는 회복이 어려운 편이에요. 필요하다면 근무 초반에만 카페인을 활용해보세요."

                    context.subjectiveFatigueLevel >= 4 ->
                        "오늘 피로감이 높은 편이에요. 필요하다면 근무 초반에만 카페인을 활용해보세요."

                    context.objectiveRecoveryLevel <= 2 ->
                        "수면 회복이 충분하지 않은 상태예요. 각성이 필요하다면 초반 시간대에만 카페인을 활용하는 것이 좋아요."

                    else ->
                        "카페인이 필요하다면 근무 초반에만 가볍게 활용해보세요."
                },
                reason = when {
                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 후 수면을 방해하지 않도록, 카페인 사용 시간을 근무 초반으로 제한했어요."

                    behavior.vulnerableShift == context.currentShift ->
                        "평소 이 근무 유형에서 회복 부담이 커지는 경향을 고려했어요."

                    context.subjectiveFatigueLevel >= 4 ->
                        "오늘 주관적 피로감이 높아, 근무 초반 각성 보조를 제안했어요."

                    context.objectiveRecoveryLevel <= 2 ->
                        "수면 회복 상태가 낮아 각성 보조가 필요할 수 있지만, 다음 수면을 위해 사용 시간을 제한했어요."

                    else ->
                        "카페인은 도움이 될 수 있지만, 늦은 섭취는 수면을 방해할 수 있어 초반으로 제한했어요."
                },
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun createMctqCaffeineAvoidIntervention(
        context: SleepInterventionContext,
        behavior: MctqBehaviorProfile
    ): List<SleepIntervention> {
        val sleepStart = context.targetSleepTime
        val caffeineCutoff = getMctqCaffeineCutoffTime(context, behavior)

        if (!caffeineCutoff.isBefore(sleepStart)) return emptyList()

        return listOf(
            SleepIntervention(
                type = InterventionType.CAFFEINE,
                startTime = caffeineCutoff,
                endTime = sleepStart,
                title = "카페인 줄이기",
                description = when {
                    behavior.hasSleepLatencyRisk ->
                        "평소 잠드는 데 시간이 오래 걸리는 편이에요. 목표 수면 전에는 카페인을 조금 더 일찍 줄여보세요."

                    behavior.hasLateNapRisk ->
                        "늦은 낮잠이나 늦은 각성 습관이 밤 수면에 영향을 줄 수 있어요. 목표 수면 전에는 카페인을 줄이는 것이 좋아요."

                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 후 회복 수면을 위해, 근무 후반부터는 카페인을 줄여보세요."

                    context.currentShift == ShiftType.EVENING ->
                        "이브닝 근무 후에는 수면 시간이 늦어질 수 있어요. 목표 수면 전에는 카페인을 줄여보세요."

                    context.currentShift == ShiftType.OFF ->
                        "쉬는 날에도 수면 리듬이 크게 흔들리지 않도록, 목표 수면 전에는 카페인을 줄여보세요."

                    else ->
                        "밤 수면을 방해하지 않도록, 목표 수면 전에는 카페인을 줄여보세요."
                },
                reason = when {
                    behavior.hasSleepLatencyRisk ->
                        "평소 수면 진입 시간이 ${behavior.averageSleepLatencyMinutes}분 정도로 나타나, 카페인 제한 시간을 조금 더 앞당겼어요."

                    behavior.hasLateNapRisk ->
                        "늦은 시간대의 각성 신호가 수면 리듬을 흔들 수 있어, 카페인 제한을 우선 배치했어요."

                    context.currentShift == ShiftType.NIGHT ->
                        "야간 근무 후 바로 회복 수면에 들어갈 수 있도록 카페인 제한 시간을 설정했어요."

                    else ->
                        "평소 수면 패턴과 목표 수면 시간을 기준으로 카페인 제한 시간을 설정했어요."
                },
                actionType = InterventionActionType.AVOID
            )
        )
    }

    private fun getMctqCaffeineCutoffTime(
        context: SleepInterventionContext,
        behavior: MctqBehaviorProfile
    ) = when {
        behavior.hasSleepLatencyRisk -> context.targetSleepTime.minusHours(8)
        behavior.hasLateNapRisk -> context.targetSleepTime.minusHours(7)
        context.currentShift == ShiftType.NIGHT -> context.targetSleepTime.minusHours(6)
        else -> context.targetSleepTime.minusHours(6)
    }

    private fun createMctqLightInterventions(
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val isShiftChanged =
            context.previousShift != null &&
                    context.previousShift != context.currentShift

        if (!isShiftChanged) return emptyList()

        return buildList {
            addAll(createMctqPreShiftLightIntervention(context, shiftRange))
            addAll(createMctqPostNightLightIntervention(context, shiftRange))
        }
    }

    private fun createMctqPreShiftLightIntervention(
        context: SleepInterventionContext,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workStart = shiftRange.startTime ?: return emptyList()

        val isTransitionToNight =
            context.currentShift == ShiftType.NIGHT &&
                    context.previousShift != null &&
                    context.previousShift != ShiftType.NIGHT

        if (!isTransitionToNight) return emptyList()

        val lightStart = when (context.chronotype) {
            Chronotype.MORNING -> workStart.minusHours(3)
            Chronotype.INTERMEDIATE -> workStart.minusHours(2)
            Chronotype.EVENING -> workStart.minusHours(2)
        }

        val lightEnd = workStart.minusMinutes(30)

        if (!lightStart.isBefore(lightEnd)) return emptyList()

        return listOf(
            SleepIntervention(
                type = InterventionType.LIGHT,
                startTime = lightStart,
                endTime = lightEnd,
                title = "근무 전 몸 깨우기",
                description =
                    "야간 근무로 전환되는 날이에요. 가능하다면 밝은 환경에서 몸을 천천히 깨워 근무 리듬에 적응해보세요.",
                reason =
                    "갑작스러운 야간 근무 전환은 몸의 리듬을 흔들 수 있어, 근무 전 각성 리듬을 천천히 올리는 시간을 배치했어요.",
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun createMctqPostNightLightIntervention(
        context: SleepInterventionContext,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workEnd = shiftRange.endTime ?: return emptyList()

        val shouldRecoverSleepSoon =
            context.currentShift == ShiftType.NIGHT &&
                    workEnd.isBefore(context.targetSleepTime)

        if (!shouldRecoverSleepSoon) return emptyList()

        return listOf(
            SleepIntervention(
                type = InterventionType.LIGHT,
                startTime = workEnd,
                endTime = context.targetSleepTime,
                title = "수면 전 자극 줄이기",
                description =
                    "퇴근 후 바로 쉬어야 하는 날이에요. 가능하다면 화면 밝기나 주변 조명을 조금 낮춰 몸이 쉴 준비를 하도록 도와주세요.",
                reason =
                    "야간 근무 후에는 몸이 아직 깨어 있으려 할 수 있어, 수면 전 자극을 줄이는 시간을 배치했어요.",
                actionType = InterventionActionType.AVOID
            )
        )
    }

    private fun getMctqPriorityScore(
        intervention: SleepIntervention,
        context: SleepInterventionContext,
        baseline: MctqBaselineProfile,
        behavior: MctqBehaviorProfile
    ): Int {
        var score = 0

        when (intervention.type) {
            InterventionType.MAIN_SLEEP -> {
                score += 200

                if (context.objectiveRecoveryLevel <= 2) score += 50
                if (context.subjectiveFatigueLevel >= 4) score += 35

                if (behavior.vulnerableShift == context.currentShift) {
                    score += 40
                }

                val currentSleepDuration = getCurrentShiftBaselineSleepMinutes(
                    context = context,
                    baseline = baseline
                )

                if (currentSleepDuration < 360) {
                    score += 30
                }
            }

            InterventionType.SLEEP_PREPARATION -> {
                score += 170

                if (behavior.hasSleepLatencyRisk) score += 45
                if (behavior.hasBedInefficiency) score += 35
                if (behavior.hasLateNapRisk) score += 25
                if (context.objectiveRecoveryLevel <= 2) score += 20
                if (context.subjectiveFatigueLevel >= 4) score += 15
            }

            InterventionType.NAP -> {
                score += 90

                if (context.currentShift == ShiftType.NIGHT) score += 40
                if (context.subjectiveFatigueLevel >= 4) score += 30
                if (context.objectiveRecoveryLevel <= 2) score += 30
                if (behavior.vulnerableShift == context.currentShift) score += 25
                if (behavior.hasNapHabit) score += 15

                if (behavior.hasLateNapRisk) {
                    score -= 30
                }
            }

            InterventionType.CAFFEINE -> {
                score += when (intervention.actionType) {
                    InterventionActionType.DO -> {
                        when {
                            context.currentShift == ShiftType.NIGHT -> 100
                            context.subjectiveFatigueLevel >= 4 -> 85
                            context.objectiveRecoveryLevel <= 2 -> 80
                            behavior.vulnerableShift == context.currentShift -> 75
                            else -> 50
                        }
                    }

                    InterventionActionType.AVOID -> {
                        when {
                            behavior.hasSleepLatencyRisk -> 110
                            behavior.hasLateNapRisk -> 95
                            context.currentShift == ShiftType.NIGHT -> 90
                            else -> 70
                        }
                    }
                }
            }

            InterventionType.LIGHT -> {
                val isShiftChanged =
                    context.previousShift != null &&
                            context.previousShift != context.currentShift

                if (!isShiftChanged) {
                    score += 10
                } else {
                    score += when (intervention.actionType) {
                        InterventionActionType.DO -> {
                            if (context.currentShift == ShiftType.NIGHT) 85 else 45
                        }

                        InterventionActionType.AVOID -> {
                            if (context.currentShift == ShiftType.NIGHT) 80 else 35
                        }
                    }
                }
            }
        }

        return score
    }

    private fun getCurrentShiftBaselineSleepMinutes(
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
}
