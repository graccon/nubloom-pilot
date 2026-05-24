package com.sujin.nubloompilot.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sujin.nubloompilot.local.SleepInterventionLocalStore
import com.sujin.nubloompilot.models.SavedSleepInterventionBundle
import kotlinx.coroutines.tasks.await

class SleepInterventionRepository(
    private val localStore: SleepInterventionLocalStore,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun save(bundle: SavedSleepInterventionBundle) {
        saveLocally(bundle)
        saveToFirestore(bundle)
    }

    fun saveLocally(bundle: SavedSleepInterventionBundle) {
        localStore.save(bundle)
    }

    suspend fun saveToFirestore(bundle: SavedSleepInterventionBundle) {
        try {
            firestore.collection("participants")
                .document(bundle.participantId)
                .collection("interventions")
                .add(bundle)
                .await()
        } catch (e: Exception) {
            // Handle error silently or log it to prevent crash
            println("Firestore save failed: ${e.message}")
        }
    }

    fun getLatestLocal(): SavedSleepInterventionBundle? {
        return localStore.getLatest()
    }

    fun clearLocal() {
        localStore.clear()
    }
}
