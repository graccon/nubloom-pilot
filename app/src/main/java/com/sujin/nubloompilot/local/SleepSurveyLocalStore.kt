package com.sujin.nubloompilot.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
) {
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

    suspend fun saveFullSleepResult(result: SleepResult) {
        context.sleepSurveyDataStore.edit { prefs ->
            prefs[lastSurveyedSleepEndTimeKey] = result.sleepEndTime.toString()
            prefs[lastMorningGloryTypeKey] = result.morningGloryType.name
            prefs[lastSleepDurationKey] = result.sleepDurationMinutes
            prefs[lastWakeHeartRateKey] = result.wakeHeartRate ?: -1L
            prefs[lastFatigueLevelKey] = result.fatigueLevel
        }
    }

    suspend fun getLatestSavedResult(): SleepResult? {
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
    suspend fun getSurveyStateForSleepSession(
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
}
