package com.sujin.nubloompilot.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sujin.nubloompilot.components.SleepReportState
import kotlinx.coroutines.flow.first
import java.time.Instant

private val Context.sleepSurveyDataStore by preferencesDataStore(
    name = "sleep_survey_store"
)

class SleepSurveyLocalStore(
    private val context: Context
) {
    private val lastSurveyedSleepEndTimeKey =
        stringPreferencesKey("last_surveyed_sleep_end_time")

    private val lastSleepReportStateKey =
        stringPreferencesKey("last_sleep_report_state")

    suspend fun saveSurveyForSleepSession(
        sleepEndTime: Instant,
        reportState: SleepReportState
    ) {
        context.sleepSurveyDataStore.edit { prefs ->
            prefs[lastSurveyedSleepEndTimeKey] = sleepEndTime.toString()
            prefs[lastSleepReportStateKey] = reportState.name
        }
    }

    suspend fun getSurveyStateForSleepSession(
        sleepEndTime: Instant
    ): SleepReportState? {
        val prefs = context.sleepSurveyDataStore.data.first()

        val savedSleepEndTime =
            prefs[lastSurveyedSleepEndTimeKey]

        val savedState =
            prefs[lastSleepReportStateKey]

        if (savedSleepEndTime != sleepEndTime.toString()) {
            return null
        }

        return savedState?.let {
            runCatching {
                SleepReportState.valueOf(it)
            }.getOrNull()
        }
    }
}