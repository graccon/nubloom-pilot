package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*

object SleepInterventionEngine {

    fun generate(
        context: SleepInterventionContext
    ): List<SleepIntervention> {
        val shiftRange = context.currentShift.getTimeRange(context.workDate)
        val candidates = buildList {
            addAll(createPreWorkLightIntervention(context, shiftRange))
            addAll(createPostWorkLightIntervention(context, shiftRange))
            addAll(createCaffeineInterventions(context, shiftRange))
            addAll(createNapInterventions(context, shiftRange))
        }
            .filter { it.startTime.isBefore(it.endTime) }
            .distinctBy { it.type to it.actionType to it.startTime to it.endTime }

        return candidates
            .sortedByDescending { getPriorityScore(it, context) }
            .take(3)
            .sortedBy { it.startTime }

    }

    private fun getPriorityScore(
        intervention: SleepIntervention,
        context: SleepInterventionContext
    ): Int {
        var score = 0

        when (intervention.type) {
            InterventionType.NAP -> {
                score += 80

                if (context.currentShift == ShiftType.NIGHT) score += 25
                if (context.subjectiveFatigueLevel >= 4) score += 20
                if (context.objectiveRecoveryLevel <= 2) score += 20

                if (
                    context.currentShift == ShiftType.NIGHT &&
                    context.previousShift != ShiftType.NIGHT
                ) {
                    score += 10
                }
            }

            InterventionType.CAFFEINE -> {
                score += when (intervention.actionType) {
                    InterventionActionType.DO -> 70
                    InterventionActionType.AVOID -> 45
                }

                if (context.currentShift == ShiftType.NIGHT) score += 15
                if (context.subjectiveFatigueLevel >= 4) score += 10
            }

            InterventionType.LIGHT -> {
                score += when (intervention.actionType) {
                    InterventionActionType.DO -> 45
                    InterventionActionType.AVOID -> 65
                }

                if (
                    context.currentShift == ShiftType.NIGHT &&
                    intervention.actionType == InterventionActionType.AVOID
                ) {
                    score += 25
                }

                if (
                    context.currentShift == ShiftType.NIGHT &&
                    intervention.actionType == InterventionActionType.DO &&
                    context.subjectiveFatigueLevel >= 4
                ) {
                    score -= 20
                }
            }
        }

        return score
    }

    private fun createPreWorkLightIntervention(
        context: SleepInterventionContext,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workStart = shiftRange.startTime

        val lightStart = when (context.chronotype) {
            Chronotype.MORNING -> context.wakeTime
            Chronotype.INTERMEDIATE -> context.wakeTime.plusMinutes(30)
            Chronotype.EVENING -> context.wakeTime.plusHours(1)
        }

        val lightEnd = minOf(
            lightStart.plusHours(2),
            workStart ?: lightStart.plusHours(2)
        )

        if (!lightStart.isBefore(lightEnd)) return emptyList()

        return listOf(
            SleepIntervention(
                type = InterventionType.LIGHT,
                startTime = lightStart,
                endTime = lightEnd,
                title = "밝은 빛 보기",
                description = "기상 후 밝은 빛을 보면 몸의 각성 리듬을 맞추는 데 도움이 될 수 있어요.",
                reason = when (context.chronotype) {
                    Chronotype.MORNING -> "아침형 리듬에 맞춰 기상 직후 빛 노출을 추천해요."
                    Chronotype.INTERMEDIATE -> "기상 후 조금씩 몸을 깨우는 데 도움이 될 수 있어요."
                    Chronotype.EVENING -> "저녁형은 각성이 늦을 수 있어, 조금 늦은 빛 노출로 리듬을 맞춰요."
                },
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun createPostWorkLightIntervention(
        context: SleepInterventionContext,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workEnd = shiftRange.endTime ?: return emptyList()

        val shouldAvoidLight =
            context.currentShift == ShiftType.NIGHT ||
                    workEnd.isAfter(context.targetSleepTime.minusHours(4))

        if (!shouldAvoidLight) return emptyList()

        val lightAvoidEnd = context.targetSleepTime

        if (!workEnd.isBefore(lightAvoidEnd)) return emptyList()

        return listOf(
            SleepIntervention(
                type = InterventionType.LIGHT,
                startTime = workEnd,
                endTime = lightAvoidEnd,
                title = "강한 빛 줄이기",
                description = "근무 후 수면을 앞두고 있다면 강한 빛 노출을 줄이는 것이 도움이 될 수 있어요.",
                reason = "밝은 빛은 몸을 깨우는 신호가 될 수 있어, 수면 전에는 자극을 낮추는 편이 좋아요.",
                actionType = InterventionActionType.AVOID
            )
        )
    }

    private fun createCaffeineInterventions(
        context: SleepInterventionContext,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val result = mutableListOf<SleepIntervention>()

        val workStart = shiftRange.startTime
        val workEnd = shiftRange.endTime

        val caffeineCutoff = when (context.chronotype) {
            Chronotype.MORNING -> context.targetSleepTime.minusHours(8)
            Chronotype.INTERMEDIATE -> context.targetSleepTime.minusHours(6)
            Chronotype.EVENING -> context.targetSleepTime.minusHours(6)
        }

        if (workStart != null && workEnd != null) {
            val caffeineUseStart = workStart
            val caffeineUseEnd = minOf(
                workStart.plusHours(3),
                workEnd,
                caffeineCutoff
            )

            if (caffeineUseStart.isBefore(caffeineUseEnd)) {
                result += SleepIntervention(
                    type = InterventionType.CAFFEINE,
                    startTime = caffeineUseStart,
                    endTime = caffeineUseEnd,
                    title = "카페인 활용 가능",
                    description = "카페인이 필요하다면 근무 초반에만 활용하는 것이 좋아요.",
                    reason = "근무 후반의 카페인은 다음 수면에 영향을 줄 수 있어요.",
                    actionType = InterventionActionType.DO
                )
            }
        }

        if (caffeineCutoff.isBefore(context.targetSleepTime)) {
            result += SleepIntervention(
                type = InterventionType.CAFFEINE,
                startTime = caffeineCutoff,
                endTime = context.targetSleepTime,
                title = "카페인 줄이기",
                description = "다음 수면을 위해 이 시간대에는 카페인을 줄이는 것이 좋아요.",
                reason = when (context.chronotype) {
                    Chronotype.MORNING -> "아침형은 늦은 카페인에 더 민감할 수 있어 조금 더 일찍 제한해요."
                    Chronotype.INTERMEDIATE -> "취침 전 카페인은 잠드는 시간을 늦출 수 있어요."
                    Chronotype.EVENING -> "저녁형이어도 취침 전 카페인은 수면 회복을 방해할 수 있어요."
                },
                actionType = InterventionActionType.AVOID
            )
        }

        return result
    }

    private fun createNapInterventions(
        context: SleepInterventionContext,
        shiftRange: ShiftTimeRange
    ): List<SleepIntervention> {
        val workStart = shiftRange.startTime

        val needsNap =
            context.subjectiveFatigueLevel >= 4 ||
                    context.objectiveRecoveryLevel <= 2 ||
                    context.currentShift == ShiftType.NIGHT

        if (!needsNap) return emptyList()

        val napStart = when (context.currentShift) {
            ShiftType.NIGHT -> workStart?.minusHours(4)
            ShiftType.EVENING -> workStart?.minusHours(3)
            ShiftType.DAY -> {
                if (context.subjectiveFatigueLevel >= 4) {
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

        return listOf(
            SleepIntervention(
                type = InterventionType.NAP,
                startTime = napStart,
                endTime = napEnd,
                title = "짧은 낮잠 추천",
                description = "이 시간대 안에서 가능하다면 20~30분 정도 짧게 쉬어보세요.",
                reason = getNapReason(context),
                actionType = InterventionActionType.DO
            )
        )
    }

    private fun getNapReason(
        context: SleepInterventionContext
    ): String {
        return when {
            context.currentShift == ShiftType.NIGHT &&
                    context.previousShift != ShiftType.NIGHT ->
                "야간 근무로 리듬이 바뀌는 날이라, 근무 전 짧은 회복 시간이 도움이 될 수 있어요."

            context.currentShift == ShiftType.NIGHT &&
                    context.nextShift == ShiftType.NIGHT ->
                "연속 야간 근무에서는 근무 전 피로 누적을 줄이는 것이 중요해요."

            context.subjectiveFatigueLevel >= 4 &&
                    context.objectiveRecoveryLevel <= 2 ->
                "주관적 피로와 수면 회복 부족 신호가 함께 나타나고 있어요."

            context.subjectiveFatigueLevel >= 4 ->
                "현재 몸이 피로를 크게 느끼고 있어요."

            else ->
                "수면 회복이 충분하지 않아 짧은 휴식이 도움이 될 수 있어요."
        }
    }
}