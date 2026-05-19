package com.sujin.nubloompilot.local

import android.content.Context
import org.json.JSONObject

class ShiftScheduleLocalStore(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "shift_schedule_prefs",
        Context.MODE_PRIVATE
    )

    fun saveSchedule(
        year: Int,
        month: Int,
        shifts: Map<Int, String>
    ) {
        val yearMonth = getYearMonthKey(year, month)

        val json = JSONObject()
        shifts.forEach { (day, shift) ->
            json.put(day.toString(), shift)
        }

        prefs.edit()
            .putString(yearMonth, json.toString())
            .apply()
    }

    fun getSchedule(
        year: Int,
        month: Int
    ): Map<Int, String> {
        val yearMonth = getYearMonthKey(year, month)
        val jsonString = prefs.getString(yearMonth, null) ?: return emptyMap()

        val json = JSONObject(jsonString)
        val result = mutableMapOf<Int, String>()

        json.keys().forEach { key ->
            result[key.toInt()] = json.getString(key)
        }

        return result
    }

    private fun getYearMonthKey(year: Int, month: Int): String {
        return "%04d-%02d".format(year, month)
    }


}