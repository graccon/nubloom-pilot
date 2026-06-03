package com.sujin.nubloompilot.repository

import com.sujin.nubloompilot.local.ISleepSurveyLocalStore
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import java.time.Instant

class FakeSleepSurveyLocalStore : ISleepSurveyLocalStore {
    private var latestResult: SleepResult? = null

    override suspend fun saveFullSleepResult(result: SleepResult) {
        latestResult = result
    }

    override suspend fun getLatestSavedResult(): SleepResult? {
        return latestResult
    }

    override suspend fun getSurveyStateForSleepSession(sleepEndTime: Instant): MorningGloryType? {
        return null // Not needed for these tests
    }
}
