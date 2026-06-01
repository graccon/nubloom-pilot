package com.sujin.nubloompilot.shared.models

data class WatchTimelinePayload(
    val yesterdayShift: String? = null,
    val todayShift: String? = null,
    val tomorrowShift: String? = null,
    val dayAfterTomorrowShift: String? = null,
    val interventions: List<WatchInterventionPayload> = emptyList(),
    val referenceDate: String,
    val updatedAt: Long
)

data class WatchInterventionPayload(
    val type: String,
    val actionType: String,
    val startTime: String,
    val endTime: String,
    val title: String,
    val description: String
)
