package com.sujin.nubloompilot.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class TimelineMarker(
    val absoluteHour: Float,
    val color: Color,
    val label: String,
    @DrawableRes val iconRes: Int
)
