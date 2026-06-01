package com.sujin.nubloompilot.wear

import android.content.Context
import android.content.SharedPreferences

class WearTimelineLocalStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTimelineInfo(
        yesterdayShift: String?,
        todayShift: String?,
        tomorrowShift: String?,
        dayAfterTomorrowShift: String?,
        referenceDate: String?,
        updatedAt: Long
    ) {
        prefs.edit().apply {
            putString(KEY_YESTERDAY_SHIFT, yesterdayShift)
            putString(KEY_TODAY_SHIFT, todayShift)
            putString(KEY_TOMORROW_SHIFT, tomorrowShift)
            putString(KEY_DAY_AFTER_TOMORROW_SHIFT, dayAfterTomorrowShift)
            putString(KEY_REFERENCE_DATE, referenceDate)
            putLong(KEY_UPDATED_AT, updatedAt)
            apply()
        }
    }

    fun getTimelineInfo(): WearTimelineInfo {
        return WearTimelineInfo(
            yesterdayShift = prefs.getString(KEY_YESTERDAY_SHIFT, null),
            todayShift = prefs.getString(KEY_TODAY_SHIFT, null),
            tomorrowShift = prefs.getString(KEY_TOMORROW_SHIFT, null),
            dayAfterTomorrowShift = prefs.getString(KEY_DAY_AFTER_TOMORROW_SHIFT, null),
            referenceDate = prefs.getString(KEY_REFERENCE_DATE, null),
            updatedAt = prefs.getLong(KEY_UPDATED_AT, 0L)
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
    }
}

data class WearTimelineInfo(
    val yesterdayShift: String?,
    val todayShift: String?,
    val tomorrowShift: String?,
    val dayAfterTomorrowShift: String?,
    val referenceDate: String?,
    val updatedAt: Long
)
