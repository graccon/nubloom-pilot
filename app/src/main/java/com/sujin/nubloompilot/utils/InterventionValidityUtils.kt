package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.SavedSleepInterventionBundle
import java.time.LocalDateTime

/**
 * Utility functions for checking the validity and active status of sleep interventions.
 */

/**
 * Checks if the [SavedSleepInterventionBundle] contains any interventions that end in the future.
 *
 * @param now The date and time to compare against. Defaults to [LocalDateTime.now].
 * @return True if the bundle is not null and has at least one intervention ending after [now].
 */
fun SavedSleepInterventionBundle?.hasActiveIntervention(
    now: LocalDateTime = LocalDateTime.now()
): Boolean {
    if (this == null) return false

    return this.interventions.any { intervention ->
        runCatching {
            LocalDateTime.parse(intervention.endTime).isAfter(now)
        }.getOrDefault(false)
    }
}
