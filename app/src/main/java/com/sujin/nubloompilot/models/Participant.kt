package com.sujin.nubloompilot.models

data class Participant(
    val participantId: String = "",
    val name: String = "",
    val birthYear: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)