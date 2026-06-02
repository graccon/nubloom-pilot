package com.sujin.nubloompilot.shared.models

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ShiftType(
    val code: String,
    val label: String,
    val koreanLabel: String,
    val color: Color
) {
    DAY("D", "Day", "주간", Color(0xFFA9C9EA)),
    EVENING("E", "Evening", "오후", Color(0xFFF4A249)),
    NIGHT("N", "Night", "야간", Color(0xFFEAB0D6)),
    OFF("OFF", "Off", "휴일", Color.Transparent);

    companion object {
        /**
         * "D", "E", "N", "OFF" 등 DB 및 전송용 코드를 기반으로 ShiftType을 찾습니다.
         */
        fun fromCode(code: String?): ShiftType {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: OFF
        }

        /**
         * "DAY", "D", "주간" 등 다양한 형태의 문자열을 ShiftType으로 변환합니다.
         */
        fun fromString(value: String?): ShiftType {
            if (value == null) return OFF
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.code.equals(value, ignoreCase = true) ||
                it.koreanLabel.equals(value, ignoreCase = true) ||
                it.label.equals(value, ignoreCase = true)
            } ?: OFF
        }
    }
}

enum class Chronotype {
    MORNING,
    INTERMEDIATE,
    EVENING
}

data class ShiftTimeRange(
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?
)

fun ShiftType.getTimeRange(
    date: LocalDate,
    config: ShiftTimingConfig = ShiftTimingConfig.Default
): ShiftTimeRange {
    return when (this) {
        ShiftType.DAY -> ShiftTimeRange(
            startTime = LocalDateTime.of(date, config.dayStart),
            endTime = LocalDateTime.of(date, config.dayStart).plusHours(config.shiftDurationHours)
        )

        ShiftType.EVENING -> ShiftTimeRange(
            startTime = LocalDateTime.of(date, config.eveningStart),
            endTime = LocalDateTime.of(date, config.eveningStart).plusHours(config.shiftDurationHours)
        )

        ShiftType.NIGHT -> ShiftTimeRange(
            startTime = LocalDateTime.of(date, config.nightStart),
            endTime = LocalDateTime.of(date, config.nightStart).plusHours(config.shiftDurationHours)
        )

        ShiftType.OFF -> ShiftTimeRange(
            startTime = null,
            endTime = null
        )
    }
}
