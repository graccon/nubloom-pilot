package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*

object SleepInterventionEngine {

    fun generate(
        context: SleepInterventionContext
    ): List<SleepIntervention> {
        val baseline = context.mctqBaselineProfile
        val behavior = context.mctqBehaviorProfile

        return if (baseline != null && behavior != null) {
            MctqInterventionFactory.create(
                context = context,
                baseline = baseline,
                behavior = behavior
            )
        } else {
            FallbackInterventionFactory.create(context)
        }
    }
}
