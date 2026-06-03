package com.sujin.nubloompilot.shared.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class TimelineMarker(
    val id: String,
    val absoluteHour: Float,
    val color: Color,
    val label: String,
    @DrawableRes val iconRes: Int,
    val endAbsoluteHour: Float? = null
)
