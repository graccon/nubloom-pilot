package com.sujin.nubloompilot.navigation

import com.sujin.nubloompilot.models.MorningGloryType

object Routes {
    const val OnboardingPage = "onboarding"
    const val SCHEDULE = "schedule"
    const val HOME = "home"
    const val MYINFO = "my_info"
    const val CHECK_IN = "check_in"

    const val SLEEP = "sleep"

    const val SLEEP_CHECK_IN =
        "sleep_check_in/{duration}/{heartRate}/{baselineDuration}/{baselineHeartRate}"
    fun sleepCheckInRoute(
        duration: Long,
        heartRate: Long?,
        baselineDuration: Long?,
        baselineHeartRate: Long?
    ): String {
        val safeHeartRate = heartRate ?: -1L
        val safeBaselineDuration = baselineDuration ?: -1L
        val safeBaselineHeartRate = baselineHeartRate ?: -1L
        return "sleep_check_in/$duration/$safeHeartRate/$safeBaselineDuration/$safeBaselineHeartRate"
    }

    const val MORNING_GLORY_RESULT = "morning_glory_result/{type}"

    fun morningGloryResultRoute(type: MorningGloryType): String = "morning_glory_result/${type.name}"
}