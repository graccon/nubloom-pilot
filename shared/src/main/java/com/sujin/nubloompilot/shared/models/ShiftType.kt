package com.sujin.nubloompilot.shared.models

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ShiftType(val label: String, val color: Color) {
    DAY("D", Color(0xFFA9C9EA)),
    EVENING("E", Color(0xFFF4A249)),
    NIGHT("N", Color(0xFFEAB0D6)),
    OFF("OFF", Color.Transparent);

    companion object {
        fun fromString(value: String?) = when(value) {
            "D" -> DAY
            "E" -> EVENING
            "N" -> NIGHT
            else -> OFF
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

fun ShiftType.getTimeRange(date: LocalDate): ShiftTimeRange {
    return when (this) {
        ShiftType.DAY -> ShiftTimeRange(
            startTime = LocalDateTime.of(date, LocalTime.of(6, 30)),
            endTime = LocalDateTime.of(date, LocalTime.of(15, 30))
        )

        ShiftType.EVENING -> ShiftTimeRange(
            startTime = LocalDateTime.of(date, LocalTime.of(14, 30)),
            endTime = LocalDateTime.of(date, LocalTime.of(23, 30))
        )

        ShiftType.NIGHT -> ShiftTimeRange(
            startTime = LocalDateTime.of(date, LocalTime.of(22, 30)),
            endTime = LocalDateTime.of(date.plusDays(1), LocalTime.of(7, 30))
        )

        ShiftType.OFF -> ShiftTimeRange(
            startTime = null,
            endTime = null
        )
    }
}
