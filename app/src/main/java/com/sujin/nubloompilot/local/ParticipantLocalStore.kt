package com.sujin.nubloompilot.local

import android.content.Context
import com.sujin.nubloompilot.models.Participant

class ParticipantLocalStore(
    context: Context
) {
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
}