package com.sujin.nubloompilot.wear

import android.content.Context
import android.content.SharedPreferences
import com.sujin.nubloompilot.shared.models.WatchInterventionPayload

class WearTimelineLocalStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTimelineInfo(
        yesterdayShift: String?,
        todayShift: String?,
        tomorrowShift: String?,
        dayAfterTomorrowShift: String?,
        referenceDate: String?,
        updatedAt: Long,
        interventions: List<WatchInterventionPayload> = emptyList()
    ) {
        prefs.edit().apply {
            putString(KEY_YESTERDAY_SHIFT, yesterdayShift)
            putString(KEY_TODAY_SHIFT, todayShift)
            putString(KEY_TOMORROW_SHIFT, tomorrowShift)
            putString(KEY_DAY_AFTER_TOMORROW_SHIFT, dayAfterTomorrowShift)
            putString(KEY_REFERENCE_DATE, referenceDate)
            putLong(KEY_UPDATED_AT, updatedAt)
            
            val serializedInterventions = interventions.map { it.toSerializedString() }.toSet()
            putStringSet(KEY_INTERVENTIONS, serializedInterventions)
            
            apply()
        }
    }

    fun getTimelineInfo(): WearTimelineInfo {
        val serializedInterventions = prefs.getStringSet(KEY_INTERVENTIONS, emptySet()) ?: emptySet()
        val interventions = serializedInterventions.mapNotNull { it.toInterventionPayload() }
            .sortedBy { it.startTime }

        return WearTimelineInfo(
            yesterdayShift = prefs.getString(KEY_YESTERDAY_SHIFT, null),
            todayShift = prefs.getString(KEY_TODAY_SHIFT, null),
            tomorrowShift = prefs.getString(KEY_TOMORROW_SHIFT, null),
            dayAfterTomorrowShift = prefs.getString(KEY_DAY_AFTER_TOMORROW_SHIFT, null),
            referenceDate = prefs.getString(KEY_REFERENCE_DATE, null),
            updatedAt = prefs.getLong(KEY_UPDATED_AT, 0L),
            interventions = interventions
        )
    }

    private fun WatchInterventionPayload.toSerializedString(): String {
        val escape = { s: String -> s.replace("|", "／") }
        return "${escape(type)}|${escape(actionType)}|${escape(startTime)}|${escape(endTime)}|${escape(title)}|${escape(description)}"
    }

    private fun String.toInterventionPayload(): WatchInterventionPayload? {
        val parts = split("|")
        if (parts.size < 6) return null
        return WatchInterventionPayload(
            type = parts[0],
            actionType = parts[1],
            startTime = parts[2],
            endTime = parts[3],
            title = parts[4],
            description = parts[5]
        )
    }

    companion object {
        private const val PREFS_NAME = "timeline_prefs"
        private const val KEY_YESTERDAY_SHIFT = "yesterday_shift"
        private const val KEY_TODAY_SHIFT = "today_shift"
        private const val KEY_TOMORROW_SHIFT = "tomorrow_shift"
        private const val KEY_DAY_AFTER_TOMORROW_SHIFT = "day_after_tomorrow_shift"
        private const val KEY_REFERENCE_DATE = "reference_date"
        private const val KEY_UPDATED_AT = "updated_at"
        private const val KEY_INTERVENTIONS = "interventions"
    }
}

data class WearTimelineInfo(
    val yesterdayShift: String?,
    val todayShift: String?,
    val tomorrowShift: String?,
    val dayAfterTomorrowShift: String?,
    val referenceDate: String?,
    val updatedAt: Long,
    val interventions: List<WatchInterventionPayload> = emptyList()
)
