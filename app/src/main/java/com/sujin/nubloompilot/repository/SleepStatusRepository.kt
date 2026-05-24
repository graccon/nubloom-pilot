package com.sujin.nubloompilot.repository

import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult

class SleepStatusRepository(
    private val healthConnectRepository: HealthConnectRepository,
    private val sleepSurveyLocalStore: SleepSurveyLocalStore
) {
    suspend fun getCurrentMorningGloryType(): MorningGloryType? {
        val latestSleepSession =
            healthConnectRepository.getLatestSleepSession()
                ?: return null

        return sleepSurveyLocalStore.getSurveyStateForSleepSession(
            sleepEndTime = latestSleepSession.endTime
        )
    }

    suspend fun getLatestSavedSleepResult(): SleepResult? {
        return sleepSurveyLocalStore.getLatestSavedResult()
    }
}
