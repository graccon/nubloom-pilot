package com.sujin.nubloompilot.shared.models

import java.time.LocalTime

/**
 * 근무 유형별 시작 시간 및 기본 근무 시간을 정의하는 설정 클래스입니다.
 */
data class ShiftTimingConfig(
    val dayStart: LocalTime = LocalTime.of(6, 30),
    val eveningStart: LocalTime = LocalTime.of(14, 30),
    val nightStart: LocalTime = LocalTime.of(22, 30),
    val shiftDurationHours: Long = 9L
) {
    companion object {
        val Default = ShiftTimingConfig()
    }
}
