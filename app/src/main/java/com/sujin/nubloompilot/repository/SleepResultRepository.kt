package com.sujin.nubloompilot.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId

class SleepResultRepository(
    private val participantId: String,
    private val localStore: com.sujin.nubloompilot.local.ISleepSurveyLocalStore,
    private val firestore: FirebaseFirestore? = FirebaseFirestore.getInstance()
) {
    private val resultsCollection = firestore?.collection("participants")
        ?.document(participantId)
        ?.collection("sleep_results")

    suspend fun saveSleepResult(result: SleepResult) {
        // 1. Check for existing result today to preserve the day's primary flower type (Method B)
        val latestResult = localStore.getLatestSavedResult()
        val finalResult = if (latestResult != null && isSameDay(latestResult.sleepEndTime, result.sleepEndTime)) {
            Log.d("SleepResultRepo", "Today's result already exists (End: ${latestResult.sleepEndTime}). " +
                    "Keeping existing MorningGloryType=${latestResult.morningGloryType}")
            result.copy(morningGloryType = latestResult.morningGloryType)
        } else {
            result
        }

        // 2. Local save for Home UI and Data Recovery
        localStore.saveFullSleepResult(finalResult)

        // 3. Remote save to Firebase (Subcollection structure: participants/{id}/sleep_results)
        val remoteData = hashMapOf(
            "participantId" to finalResult.participantId,
            "participantName" to finalResult.participantName,
            "sleepEndTime" to finalResult.sleepEndTime.toString(),
            "sleepDurationMinutes" to finalResult.sleepDurationMinutes,
            "wakeHeartRate" to (finalResult.wakeHeartRate ?: -1L),
            "fatigueLevel" to finalResult.fatigueLevel,
            "morningGloryType" to finalResult.morningGloryType.name,
            "timestamp" to finalResult.timestamp,
            "sleepSummary" to finalResult.sleepSummary?.let { summary ->
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
            },
            "sleepSummariesLast24h" to finalResult.sleepSummariesLast24h.map { summary ->
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
            if (resultsCollection != null) {
                resultsCollection.add(remoteData).await()
                Log.d("SleepResultRepo", "Firestore save success. Final type: ${finalResult.morningGloryType}")
            } else {
                Log.w("SleepResultRepo", "resultsCollection is null, skipping remote save")
            }
        } catch (e: Exception) {
            Log.e("SleepResultRepo", "Firestore save failed", e)
        }
    }

    private fun isSameDay(instant1: Instant, instant2: Instant): Boolean {
        val date1 = instant1.atZone(ZoneId.systemDefault()).toLocalDate()
        val date2 = instant2.atZone(ZoneId.systemDefault()).toLocalDate()
        return date1 == date2
    }

    suspend fun getSleepResultsInDateRange(days: Int = 30): List<SleepResult> {
        val collection = resultsCollection ?: run {
            Log.w("SleepResultRepo", "getSleepResultsInDateRange: resultsCollection is null")
            return emptyList()
        }
        return try {
            val since = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val snapshot = collection
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
        val collection = resultsCollection ?: run {
            Log.w("SleepResultRepo", "getLatestSavedSleepResult: resultsCollection is null")
            return null
        }
        return try {
            val snapshot = collection
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

    suspend fun getCachedLatestSummary(): com.sujin.nubloompilot.models.DailyHealthSummary? {
        return localStore.getCachedLatestSummary()
    }

    suspend fun saveLatestSummaryCache(summary: com.sujin.nubloompilot.models.DailyHealthSummary) {
        localStore.saveLatestSummaryCache(summary)
    }
}
