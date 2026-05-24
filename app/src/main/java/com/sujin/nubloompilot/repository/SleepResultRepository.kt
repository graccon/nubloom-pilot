package com.sujin.nubloompilot.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.SleepResult
import kotlinx.coroutines.tasks.await

class SleepResultRepository(
    private val localStore: SleepSurveyLocalStore,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveSleepResult(result: SleepResult) {
        // 1. Local save for Home UI
        localStore.saveSurveyForSleepSession(
            sleepEndTime = result.sleepEndTime,
            morningGloryType = result.morningGloryType
        )

        // 2. Remote save to Firebase
        val remoteData = hashMapOf(
            "participantId" to result.participantId,
            "participantName" to result.participantName,
            "sleepEndTime" to result.sleepEndTime.toString(),
            "sleepDurationMinutes" to result.sleepDurationMinutes,
            "wakeHeartRate" to (result.wakeHeartRate ?: -1L),
            "fatigueLevel" to result.fatigueLevel,
            "morningGloryType" to result.morningGloryType.name,
            "timestamp" to result.timestamp
        )

        firestore.collection("sleep_results")
            .add(remoteData)
            .await()
    }
}
