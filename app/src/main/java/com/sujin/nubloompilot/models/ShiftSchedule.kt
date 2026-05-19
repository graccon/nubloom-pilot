package com.sujin.nubloompilot.models

data class ShiftSchedule(
    val year: Int = 0,
    val month: Int = 0,
    val shifts: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)