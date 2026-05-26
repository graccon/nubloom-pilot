package com.sujin.nubloompilot.local

import android.content.Context
import com.google.gson.Gson
import com.sujin.nubloompilot.models.BaselineAssessment
import com.sujin.nubloompilot.models.Participant

class ParticipantLocalStore(
    context: Context
) {
    private val gson = Gson()
    private val KEY_BASELINE_ASSESSMENT = "baseline_assessment_json"

    private val prefs = context.getSharedPreferences(
        "participant_prefs",
        Context.MODE_PRIVATE
    )

    fun saveParticipant(participant: Participant) {
        prefs.edit()
            .putString("participantId", participant.participantId)
            .putString("name", participant.name)
            .putInt("birthYear", participant.birthYear)
            .putLong("createdAt", participant.createdAt)
            .apply()
    }

    fun getParticipantId(): String? {
        return prefs.getString("participantId", null)
    }

    fun getParticipantName(): String? {
        return prefs.getString("name", null)
    }

    fun saveBaselineAssessment(assessment: BaselineAssessment) {
        val json = gson.toJson(assessment)
        prefs.edit()
            .putString(KEY_BASELINE_ASSESSMENT, json)
            .apply()
    }

    fun getBaselineAssessment(): BaselineAssessment? {
        val json = prefs.getString(KEY_BASELINE_ASSESSMENT, null) ?: return null
        return runCatching {
            gson.fromJson(json, BaselineAssessment::class.java)
        }.getOrNull()
    }
}