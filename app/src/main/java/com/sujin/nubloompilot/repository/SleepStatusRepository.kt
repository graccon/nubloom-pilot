package com.sujin.nubloompilot.repository

import com.sujin.nubloompilot.components.SleepReportState
import com.sujin.nubloompilot.local.SleepSurveyLocalStore

class SleepStatusRepository(
    private val healthConnectRepository: HealthConnectRepository,
    private val sleepSurveyLocalStore: SleepSurveyLocalStore
) {
    suspend fun getCurrentSleepReportState(): SleepReportState {
        val latestSleepSession =
            healthConnectRepository.getLatestSleepSession()
                ?: return SleepReportState.NONE

        return sleepSurveyLocalStore.getSurveyStateForSleepSession(
            sleepEndTime = latestSleepSession.endTime
        ) ?: SleepReportState.NONE
    }
}