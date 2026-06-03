package com.sujin.nubloompilot.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant

private val Context.sleepSurveyDataStore by preferencesDataStore(
    name = "sleep_survey_store"
)

class SleepSurveyLocalStore(
    private val context: Context
) : ISleepSurveyLocalStore {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, com.google.gson.JsonSerializer<Instant> { src, _, _ ->
            JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(Instant::class.java, com.google.gson.JsonDeserializer { json, _, _ ->
            Instant.parse(json.asString)
        })
        .create()

    private val lastSurveyedSleepEndTimeKey =
        stringPreferencesKey("last_surveyed_sleep_end_time")

    private val lastMorningGloryTypeKey =
        stringPreferencesKey("last_morning_glory_type")

    private val lastSleepDurationKey =
        longPreferencesKey("last_sleep_duration")

    private val lastWakeHeartRateKey =
        longPreferencesKey("last_wake_heart_rate")

    private val lastFatigueLevelKey =
        intPreferencesKey("last_fatigue_level")

    private val latestSummaryCacheKey =
        stringPreferencesKey("latest_summary_cache")

    override suspend fun saveFullSleepResult(result: SleepResult) {
        context.sleepSurveyDataStore.edit { prefs ->
            prefs[lastSurveyedSleepEndTimeKey] = result.sleepEndTime.toString()
            prefs[lastMorningGloryTypeKey] = result.morningGloryType.name
            prefs[lastSleepDurationKey] = result.sleepDurationMinutes
            prefs[lastWakeHeartRateKey] = result.wakeHeartRate ?: -1L
            prefs[lastFatigueLevelKey] = result.fatigueLevel
        }
    }

    override suspend fun getLatestSavedResult(): SleepResult? {
        val prefs = context.sleepSurveyDataStore.data.first()
        val endTimeStr = prefs[lastSurveyedSleepEndTimeKey] ?: return null
        val typeStr = prefs[lastMorningGloryTypeKey] ?: return null
        
        val type = runCatching { MorningGloryType.valueOf(typeStr) }.getOrNull() ?: return null
        val endTime = runCatching { Instant.parse(endTimeStr) }.getOrNull() ?: return null
        
        val heartRate = prefs[lastWakeHeartRateKey]?.takeIf { it != -1L }

        return SleepResult(
            participantId = "local", // Not strictly needed for UI recovery
            participantName = "",    // Not strictly needed for UI recovery
            sleepEndTime = endTime,
            sleepDurationMinutes = prefs[lastSleepDurationKey] ?: 0L,
            wakeHeartRate = heartRate,
            fatigueLevel = prefs[lastFatigueLevelKey] ?: 0,
            morningGloryType = type
        )
    }

    // Improved check to avoid redundant surveys for fragmented data.
    override suspend fun getSurveyStateForSleepSession(
        sleepEndTime: Instant
    ): MorningGloryType? {
        val prefs = context.sleepSurveyDataStore.data.first()
        val savedSleepEndTimeStr = prefs[lastSurveyedSleepEndTimeKey] ?: return null
        val savedState = prefs[lastMorningGloryTypeKey]

        val savedEndTime = runCatching { Instant.parse(savedSleepEndTimeStr) }.getOrNull() ?: return null

        // If the new detected end time is within 8 hours of the already surveyed time,
        // we assume it's part of the same sleep episode (incremental sync).
        val diffMinutes = Duration.between(savedEndTime, sleepEndTime).abs().toMinutes()
        
        if (diffMinutes > 480) { // 8 hours threshold
            return null
        }

        return savedState?.let {
            runCatching { MorningGloryType.valueOf(it) }.getOrNull()
        }
    }

    override suspend fun saveLatestSummaryCache(summary: DailyHealthSummary) {
        context.sleepSurveyDataStore.edit { prefs ->
            prefs[latestSummaryCacheKey] = gson.toJson(summary)
        }
    }

    override suspend fun getCachedLatestSummary(): DailyHealthSummary? {
        val prefs = context.sleepSurveyDataStore.data.first()
        val json = prefs[latestSummaryCacheKey] ?: return null
        return runCatching {
            gson.fromJson(json, DailyHealthSummary::class.java)
        }.getOrNull()
    }

    override suspend fun clearLatestSummaryCache() {
        context.sleepSurveyDataStore.edit { prefs ->
            prefs.remove(latestSummaryCacheKey)
        }
    }
}
