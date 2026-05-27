package com.sujin.nubloompilot.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.models.BaselineAssessment
import com.sujin.nubloompilot.models.MctqBaselineProfile
import com.sujin.nubloompilot.models.Participant
import java.util.UUID

class ParticipantRepository(
    context: Context
) {
    private val db = FirebaseFirestore.getInstance()
    private val localStore = ParticipantLocalStore(context)

    fun registerParticipant(
        name: String,
        birthYear: Int,
        assessment: BaselineAssessment? = null,
        baselineProfile: MctqBaselineProfile? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val participant = Participant(
            participantId = UUID.randomUUID().toString(),
            name = name,
            birthYear = birthYear
        )

        localStore.saveParticipant(participant)
        assessment?.let { localStore.saveBaselineAssessment(it) }
        baselineProfile?.let { localStore.saveBaselineProfile(it) }

        val docRef = db.collection("participants")
            .document(participant.participantId)

        // Use map to include assessment data without modifying existing Participant model
        val data = mutableMapOf<String, Any?>(
            "participantId" to participant.participantId,
            "name" to participant.name,
            "birthYear" to participant.birthYear,
            "createdAt" to participant.createdAt
        )
        if (assessment != null) {
            data["assessment"] = assessment
        }
        if (baselineProfile != null) {
            data["baselineProfile"] = baselineProfile
        }

        docRef.set(data)
            .addOnSuccessListener {
                Log.d("OnboardingDebug", "Firestore save success")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("OnboardingDebug", "Firestore save failed", exception)
                onFailure(exception)
            }
    }
}