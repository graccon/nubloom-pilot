package com.sujin.nubloompilot.local

import android.content.Context
import com.google.gson.Gson
import com.sujin.nubloompilot.models.SavedSleepInterventionBundle

class SleepInterventionLocalStore(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "sleep_intervention_prefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val key = "latest_sleep_intervention_bundle"

    fun save(bundle: SavedSleepInterventionBundle) {
        val json = gson.toJson(bundle)
        prefs.edit()
            .putString(key, json)
            .apply()
    }

    fun getLatest(): SavedSleepInterventionBundle? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, SavedSleepInterventionBundle::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit()
            .remove(key)
            .apply()
    }
}
