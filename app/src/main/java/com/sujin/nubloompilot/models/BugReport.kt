package com.sujin.nubloompilot.models

/**
 * Data model for pilot test bug reports.
 */
data class BugReport(
    val participantId: String,
    val participantName: String,
    val description: String,
    val createdAt: String,
    val appVersion: String? = null,
    val deviceModel: String? = null,
    val osVersion: String? = null
)
