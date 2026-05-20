package com.sujin.nubloompilot.components

enum class TimelineStartPeriod(
    val startHour: Float
) {

    AM(startHour = 0f),
    PM(startHour = 12f)
}