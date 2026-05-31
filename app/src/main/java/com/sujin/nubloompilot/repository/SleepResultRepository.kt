package com.sujin.nubloompilot.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import kotlinx.coroutines.tasks.await
import java.time.Instant

class SleepResultRepository(
    private val participantId: String,
    private val localStore: SleepSurveyLocalStore,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val resultsCollection = firestore.collection("participants")
        .document(participantId)
        .collection("sleep_results")

    suspend fun saveSleepResult(result: SleepResult) {
        // 1. Local save for Home UI and Data Recovery
        localStore.saveFullSleepResult(result)

        // 2. Remote save to Firebase (Subcollection structure: participants/{id}/sleep_results)
        val remoteData = hashMapOf(
            "participantId" to result.participantId,
            "participantName" to result.participantName,
            "sleepEndTime" to result.sleepEndTime.toString(),
            "sleepDurationMinutes" to result.sleepDurationMinutes,
            "wakeHeartRate" to (result.wakeHeartRate ?: -1L),
            "fatigueLevel" to result.fatigueLevel,
            "morningGloryType" to result.morningGloryType.name,
            "timestamp" to result.timestamp,
            "sleepSummary" to result.sleepSummary?.let { summary ->
                hashMapOf(
                    "sleepStartTime" to summary.sleepStartTime.toString(),
                    "sleepEndTime" to summary.sleepEndTime.toString(),
                    "sleepDurationMinutes" to summary.sleepDurationMinutes,
                    "lightSleepMinutes" to summary.lightSleepMinutes,
                    "deepSleepMinutes" to summary.deepSleepMinutes,
                    "remSleepMinutes" to summary.remSleepMinutes,
                    "awakeSleepMinutes" to summary.awakeSleepMinutes,
                    "wakeHeartRate" to (summary.wakeHeartRate ?: -1L),
                    "averageHrvMillis" to (summary.averageHrvMillis ?: -1),
                    "stepsLast24Hours" to summary.stepsLast24Hours
                )
            }
        )

        try {
            resultsCollection.add(remoteData).await()
            println("Firestore sleep_results save success with summary: ${result.sleepSummary != null}")
        } catch (e: Exception) {
            println("Firestore sleep_results save failed: ${e.message}")
        }
    }

    suspend fun getSleepResultsInDateRange(days: Int = 30): List<SleepResult> {
        return try {
            val since = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val snapshot = resultsCollection
                .whereGreaterThanOrEqualTo("timestamp", since)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val typeStr = doc.getString("morningGloryType") ?: return@mapNotNull null
                val endTimeStr = doc.getString("sleepEndTime") ?: return@mapNotNull null
                SleepResult(
                    participantId = doc.getString("participantId") ?: participantId,
                    participantName = doc.getString("participantName") ?: "",
                    sleepEndTime = Instant.parse(endTimeStr),
                    sleepDurationMinutes = doc.getLong("sleepDurationMinutes") ?: 0L,
                    wakeHeartRate = doc.getLong("wakeHeartRate")?.takeIf { it != -1L },
                    fatigueLevel = doc.getLong("fatigueLevel")?.toInt() ?: 0,
                    morningGloryType = MorningGloryType.valueOf(typeStr),
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }
        } catch (e: Exception) {
            println("Firestore getSleepResultsInDateRange failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getLatestSavedSleepResult(): SleepResult? {
        return try {
            val snapshot = resultsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val doc = snapshot.documents.first()
            val typeStr = doc.getString("morningGloryType") ?: return null
            val endTimeStr = doc.getString("sleepEndTime") ?: return null

            SleepResult(
                participantId = doc.getString("participantId") ?: participantId,
                participantName = doc.getString("participantName") ?: "",
                sleepEndTime = Instant.parse(endTimeStr),
                sleepDurationMinutes = doc.getLong("sleepDurationMinutes") ?: 0L,
                wakeHeartRate = doc.getLong("wakeHeartRate")?.takeIf { it != -1L },
                fatigueLevel = doc.getLong("fatigueLevel")?.toInt() ?: 0,
                morningGloryType = MorningGloryType.valueOf(typeStr),
                timestamp = doc.getLong("timestamp") ?: 0L
            )
        } catch (e: Exception) {
            println("Firestore getLatestSavedSleepResult failed: ${e.message}")
            null
        }
    }
}
