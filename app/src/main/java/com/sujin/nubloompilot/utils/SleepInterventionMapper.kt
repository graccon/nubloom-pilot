package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import java.time.Instant

object SleepInterventionMapper {

    fun toBundle(
        participantId: String,
        morningGloryType: MorningGloryType,
        context: SleepInterventionContext,
        interventions: List<SleepIntervention>
    ): SavedSleepInterventionBundle {
        return SavedSleepInterventionBundle(
            participantId = participantId,
            generatedAt = Instant.now().toString(),
            workDate = context.workDate.toString(),
            morningGloryType = morningGloryType.name,
            chronotype = context.chronotype.name,
            previousShift = context.previousShift?.label ?: "OFF",
            currentShift = context.currentShift.label,
            nextShift = context.nextShift?.label ?: "OFF",
            interventions = interventions.map { intervention ->
                SavedSleepIntervention(
                    type = intervention.type.name,
                    actionType = intervention.actionType.name,
                    startTime = intervention.startTime.toString(),
                    endTime = intervention.endTime.toString(),
                    title = intervention.title,
                    description = intervention.description,
                    reason = intervention.reason
                )
            }
        )
    }
}
