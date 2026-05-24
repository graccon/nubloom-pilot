package com.sujin.nubloompilot.models

data class SavedSleepIntervention(
    val type: String,
    val actionType: String,
    val startTime: String,
    val endTime: String,
    val title: String,
    val description: String,
    val reason: String
)

data class SavedSleepInterventionBundle(
    val participantId: String,
    val generatedAt: String,
    val workDate: String,
    val morningGloryType: String,
    val chronotype: String,
    val previousShift: String,
    val currentShift: String,
    val nextShift: String,
    val interventions: List<SavedSleepIntervention>
)
