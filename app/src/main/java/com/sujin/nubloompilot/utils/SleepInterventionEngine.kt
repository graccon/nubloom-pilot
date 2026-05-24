package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import java.time.LocalDateTime

object SleepInterventionEngine {

    fun generate(
        context: SleepInterventionContext
    ): List<SleepIntervention> {
        val interventions = mutableListOf<SleepIntervention>()

        interventions += generateLightInterventions(context)
        interventions += generateCaffeineInterventions(context)
        interventions += generateNapInterventions(context)

        return interventions
            .distinctBy { it.type to it.startTime to it.endTime to it.actionType }
            .sortedBy { it.startTime }
    }

    private fun generateLightInterventions(
        context: SleepInterventionContext
    ): List<SleepIntervention> {
        val result = mutableListOf<SleepIntervention>()

        val lightStart = when (context.chronotype) {
            Chronotype.MORNING -> context.wakeTime
            Chronotype.INTERMEDIATE -> context.wakeTime.plusMinutes(30)
            Chronotype.EVENING -> context.wakeTime.plusHours(1)
        }

        result += SleepIntervention(
            type = InterventionType.LIGHT,
            startTime = lightStart,
            endTime = lightStart.plusHours(2),
            title = "밝은 빛 보기",
            description = "기상 후 밝은 빛을 보면 몸이 깨어나는 신호를 더 잘 받을 수 있어요.",
            reason = when (context.chronotype) {
                Chronotype.MORNING -> "아침형 리듬에 맞춰 기상 직후 빛 노출을 추천해요."
                Chronotype.INTERMEDIATE -> "기상 후 조금씩 각성 리듬을 올리는 데 도움이 돼요."
                Chronotype.EVENING -> "저녁형은 아침 각성이 늦어질 수 있어, 조금 늦은 빛 노출로 리듬을 맞춰요."
            },
            actionType = InterventionActionType.DO
        )

        if (context.currentShift == ShiftType.NIGHT && context.workEndTime != null) {
            result += SleepIntervention(
                type = InterventionType.LIGHT,
                startTime = context.workEndTime,
                endTime = context.workEndTime.plusHours(2),
                title = "강한 빛 피하기",
                description = "야간 근무 후에는 밝은 빛을 줄여야 퇴근 후 수면에 들어가기 쉬워요.",
                reason = "야간 근무 후 아침 빛을 많이 보면 잠드는 시간이 늦어질 수 있어요.",
                actionType = InterventionActionType.AVOID
            )
        }

        return result
    }

    private fun generateCaffeineInterventions(
        context: SleepInterventionContext
    ): List<SleepIntervention> {
        val result = mutableListOf<SleepIntervention>()

        val caffeineCutoffStart = when (context.chronotype) {
            Chronotype.MORNING -> context.targetSleepTime.minusHours(8)
            Chronotype.INTERMEDIATE -> context.targetSleepTime.minusHours(6)
            Chronotype.EVENING -> context.targetSleepTime.minusHours(6)
        }

        if (context.workStartTime != null) {
            val caffeineStart = context.workStartTime
            val caffeineEnd = minOf(
                context.workStartTime.plusHours(3),
                caffeineCutoffStart
            )

            if (caffeineStart.isBefore(caffeineEnd)) {
                result += SleepIntervention(
                    type = InterventionType.CAFFEINE,
                    startTime = caffeineStart,
                    endTime = caffeineEnd,
                    title = "카페인 활용 가능",
                    description = "카페인이 필요하다면 근무 초반에 활용하는 것이 좋아요.",
                    reason = "근무 후반보다 초반에 섭취해야 다음 수면에 영향을 덜 줄 수 있어요.",
                    actionType = InterventionActionType.DO
                )
            }
        }

        result += SleepIntervention(
            type = InterventionType.CAFFEINE,
            startTime = caffeineCutoffStart,
            endTime = context.targetSleepTime,
            title = "카페인 피하기",
            description = "다음 수면을 위해 이 시간대에는 카페인을 줄이는 것이 좋아요.",
            reason = when (context.chronotype) {
                Chronotype.MORNING -> "아침형은 늦은 카페인에 더 민감할 수 있어 조금 더 일찍 제한해요."
                Chronotype.INTERMEDIATE -> "취침 전 카페인은 잠드는 시간을 늦출 수 있어요."
                Chronotype.EVENING -> "저녁형이어도 취침 전 카페인은 수면 회복을 방해할 수 있어요."
            },
            actionType = InterventionActionType.AVOID
        )

        return result
    }

    private fun generateNapInterventions(
        context: SleepInterventionContext
    ): List<SleepIntervention> {
        val result = mutableListOf<SleepIntervention>()

        val needsNap =
            context.subjectiveFatigueLevel >= 4 ||
                    context.objectiveRecoveryLevel <= 2 ||
                    context.currentShift == ShiftType.NIGHT

        if (!needsNap) return result

        if (context.workStartTime != null) {
            val napStart = when (context.currentShift) {
                ShiftType.NIGHT -> context.workStartTime.minusHours(4)
                ShiftType.EVENING -> context.workStartTime.minusHours(3)
                ShiftType.DAY -> context.workStartTime.minusHours(2)
                ShiftType.OFF -> context.wakeTime.plusHours(3)
            }

            result += SleepIntervention(
                type = InterventionType.NAP,
                startTime = napStart,
                endTime = napStart.plusHours(1),
                title = "짧은 낮잠 추천",
                description = "가능하다면 이 시간대에 20~30분 정도 짧게 쉬어보세요.",
                reason = getNapReason(context),
                actionType = InterventionActionType.DO
            )
        } else {
            val napStart = context.wakeTime.plusHours(4)

            result += SleepIntervention(
                type = InterventionType.NAP,
                startTime = napStart,
                endTime = napStart.plusHours(1),
                title = "짧은 회복 낮잠",
                description = "피로가 크다면 20~30분 정도 짧게 쉬어가도 좋아요.",
                reason = "오늘은 회복이 더 필요한 상태로 보여요.",
                actionType = InterventionActionType.DO
            )
        }

        return result
    }

    private fun getNapReason(
        context: SleepInterventionContext
    ): String {
        return when {
            context.currentShift == ShiftType.NIGHT &&
                    context.previousShift != ShiftType.NIGHT ->
                "근무 리듬이 야간으로 바뀌는 날이라, 근무 전 회복 시간이 중요해요."

            context.currentShift == ShiftType.NIGHT ->
                "야간 근무 중 피로 누적을 줄이기 위해 근무 전 짧은 낮잠이 도움이 될 수 있어요."

            context.subjectiveFatigueLevel >= 4 &&
                    context.objectiveRecoveryLevel <= 2 ->
                "주관적 피로와 수면 회복 부족이 함께 나타나고 있어요."

            context.subjectiveFatigueLevel >= 4 ->
                "현재 몸이 피로를 크게 느끼고 있어요."

            else ->
                "수면 회복이 충분하지 않아 짧은 휴식이 도움이 될 수 있어요."
        }
    }
}