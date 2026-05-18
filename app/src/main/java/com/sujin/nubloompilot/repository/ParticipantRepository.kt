package com.sujin.nubloompilot.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.sujin.nubloompilot.local.ParticipantLocalStore
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
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val participant = Participant(
            participantId = UUID.randomUUID().toString(),
            name = name,
            birthYear = birthYear
        )

        localStore.saveParticipant(participant)

        db.collection("participants")
            .document(participant.participantId)
            .set(participant)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}