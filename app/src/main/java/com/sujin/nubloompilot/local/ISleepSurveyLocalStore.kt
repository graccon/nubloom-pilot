package com.sujin.nubloompilot.local

import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import java.time.Instant

interface ISleepSurveyLocalStore {
    suspend fun saveFullSleepResult(result: SleepResult)
    suspend fun getLatestSavedResult(): SleepResult?
    suspend fun getSurveyStateForSleepSession(sleepEndTime: Instant): MorningGloryType?
}
