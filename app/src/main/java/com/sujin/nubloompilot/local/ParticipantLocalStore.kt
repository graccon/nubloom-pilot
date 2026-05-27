package com.sujin.nubloompilot.local

import android.content.Context
import com.google.gson.Gson
import com.sujin.nubloompilot.models.BaselineAssessment
import com.sujin.nubloompilot.models.MctqBaselineProfile
import com.sujin.nubloompilot.models.MctqBehaviorProfile
import com.sujin.nubloompilot.models.Participant

class ParticipantLocalStore(
    context: Context
) {
    private val gson = Gson()
    private val KEY_BASELINE_ASSESSMENT = "baseline_assessment_json"
    private val KEY_BASELINE_PROFILE = "baseline_profile_json"
    private val KEY_MCTQ_BEHAVIOR_PROFILE = "mctq_behavior_profile_json"

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

    fun saveBaselineProfile(profile: MctqBaselineProfile) {
        val json = gson.toJson(profile)
        prefs.edit()
            .putString(KEY_BASELINE_PROFILE, json)
            .apply()
    }

    fun getBaselineProfile(): MctqBaselineProfile? {
        val json = prefs.getString(KEY_BASELINE_PROFILE, null) ?: return null
        return runCatching {
            gson.fromJson(json, MctqBaselineProfile::class.java)
        }.getOrNull()
    }

    fun saveMctqBehaviorProfile(profile: MctqBehaviorProfile) {
        val json = gson.toJson(profile)
        prefs.edit()
            .putString(KEY_MCTQ_BEHAVIOR_PROFILE, json)
            .apply()
    }

    fun getMctqBehaviorProfile(): MctqBehaviorProfile? {
        val json = prefs.getString(KEY_MCTQ_BEHAVIOR_PROFILE, null) ?: return null
        return runCatching {
            gson.fromJson(json, MctqBehaviorProfile::class.java)
        }.getOrNull()
    }
}