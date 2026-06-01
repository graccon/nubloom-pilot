package com.sujin.nubloompilot.wear

import com.sujin.nubloompilot.models.SavedSleepIntervention
import com.sujin.nubloompilot.shared.models.WatchInterventionPayload
import com.sujin.nubloompilot.shared.models.WatchTimelinePayload
import java.time.LocalDate

object WatchTimelinePayloadMapper {
    fun map(
        yesterdayShift: String?,
        todayShift: String?,
        tomorrowShift: String?,
        dayAfterTomorrowShift: String?,
        interventions: List<SavedSleepIntervention>
    ): WatchTimelinePayload {
        return WatchTimelinePayload(
            yesterdayShift = yesterdayShift,
            todayShift = todayShift,
            tomorrowShift = tomorrowShift,
            dayAfterTomorrowShift = dayAfterTomorrowShift,
            interventions = interventions.map { it.toWatchPayload() },
            referenceDate = LocalDate.now().toString(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun SavedSleepIntervention.toWatchPayload(): WatchInterventionPayload {
        return WatchInterventionPayload(
            type = type,
            actionType = actionType,
            startTime = startTime,
            endTime = endTime,
            title = title,
            description = description
        )
    }
}
