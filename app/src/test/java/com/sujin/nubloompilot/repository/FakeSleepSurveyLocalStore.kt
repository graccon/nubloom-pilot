package com.sujin.nubloompilot.repository

import com.sujin.nubloompilot.local.ISleepSurveyLocalStore
import com.sujin.nubloompilot.models.DailyHealthSummary
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import java.time.Instant

class FakeSleepSurveyLocalStore : ISleepSurveyLocalStore {
    private var latestResult: SleepResult? = null
    private var latestSummaryCache: DailyHealthSummary? = null

    override suspend fun saveFullSleepResult(result: SleepResult) {
        latestResult = result
    }

    override suspend fun getLatestSavedResult(): SleepResult? {
        return latestResult
    }

    override suspend fun getSurveyStateForSleepSession(
        sleepEndTime: Instant
    ): MorningGloryType? {
        return null
    }

    override suspend fun saveLatestSummaryCache(
        summary: DailyHealthSummary
    ) {
        latestSummaryCache = summary
    }

    override suspend fun getCachedLatestSummary(): DailyHealthSummary? {
        return latestSummaryCache
    }

    override suspend fun clearLatestSummaryCache() {
        latestSummaryCache = null
    }
}