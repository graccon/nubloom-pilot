package com.sujin.nubloompilot.repository

import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.MorningGloryType

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
}
