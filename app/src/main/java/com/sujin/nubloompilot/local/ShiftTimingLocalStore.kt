package com.sujin.nubloompilot.local

import android.content.Context
import com.sujin.nubloompilot.shared.models.ShiftTimingConfig
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 사용자가 설정한 근무 시작 시간 및 지속 시간을 기기 로컬에 저장하고 불러오는 클래스입니다.
 */
class ShiftTimingLocalStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 현재 저장된 근무 시간 설정을 가져옵니다. 저장된 값이 없으면 기본값을 반환합니다.
     */
    fun getConfig(): ShiftTimingConfig {
        val dayStartStr = prefs.getString(KEY_DAY_START, null)
        val eveningStartStr = prefs.getString(KEY_EVENING_START, null)
        val nightStartStr = prefs.getString(KEY_NIGHT_START, null)
        val duration = prefs.getLong(KEY_SHIFT_DURATION_HOURS, ShiftTimingConfig.Default.shiftDurationHours)

        return ShiftTimingConfig(
            dayStart = safeParseTime(dayStartStr, ShiftTimingConfig.Default.dayStart),
            eveningStart = safeParseTime(eveningStartStr, ShiftTimingConfig.Default.eveningStart),
            nightStart = safeParseTime(nightStartStr, ShiftTimingConfig.Default.nightStart),
            shiftDurationHours = duration
        )
    }

    /**
     * 새로운 근무 시간 설정을 저장합니다.
     */
    fun saveConfig(config: ShiftTimingConfig) {
        prefs.edit().apply {
            putString(KEY_DAY_START, config.dayStart.format(timeFormatter))
            putString(KEY_EVENING_START, config.eveningStart.format(timeFormatter))
            putString(KEY_NIGHT_START, config.nightStart.format(timeFormatter))
            putLong(KEY_SHIFT_DURATION_HOURS, config.shiftDurationHours)
            apply()
        }
    }

    /**
     * 설정을 초기 기본값으로 되돌립니다.
     */
    fun resetToDefault() {
        prefs.edit().clear().apply()
    }

    /**
     * 사용자가 변경한 커스텀 설정이 존재하는지 확인합니다.
     */
    fun hasCustomConfig(): Boolean {
        return prefs.contains(KEY_DAY_START) || 
               prefs.contains(KEY_EVENING_START) || 
               prefs.contains(KEY_NIGHT_START) || 
               prefs.contains(KEY_SHIFT_DURATION_HOURS)
    }

    private fun safeParseTime(timeStr: String?, fallback: LocalTime): LocalTime {
        if (timeStr.isNullOrBlank()) return fallback
        return try {
            LocalTime.parse(timeStr, timeFormatter)
        } catch (e: Exception) {
            fallback
        }
    }

    companion object {
        private const val PREF_NAME = "shift_timing_preferences"
        private const val KEY_DAY_START = "day_start"
        private const val KEY_EVENING_START = "evening_start"
        private const val KEY_NIGHT_START = "night_start"
        private const val KEY_SHIFT_DURATION_HOURS = "shift_duration_hours"
    }
}
