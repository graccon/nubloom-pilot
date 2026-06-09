package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawSleepWindowBar(
    startHour: Float,
    endHour: Float,
    y: Float,
    height: Float,
    plotLeft: Float,
    plotWidth: Float,
    color: Color,
    alpha: Float
) {
    val cornerRadius = CornerRadius(height / 2, height / 2)
    
    if (startHour < endHour) {
        val xStart = plotLeft + (startHour / 24f) * plotWidth
        val xEnd = plotLeft + (endHour / 24f) * plotWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(xStart, y),
            size = Size(xEnd - xStart, height),
            cornerRadius = cornerRadius,
            alpha = alpha
        )
    } else {
        // Wrap around midnight
        // 1. From start to 24h
        val xStart1 = plotLeft + (startHour / 24f) * plotWidth
        val xEnd1 = plotLeft + plotWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(xStart1, y),
            size = Size(xEnd1 - xStart1, height),
            cornerRadius = cornerRadius,
            alpha = alpha
        )
        // 2. From 0h to end
        val xStart2 = plotLeft
        val xEnd2 = plotLeft + (endHour / 24f) * plotWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(xStart2, y),
            size = Size(xEnd2 - xStart2, height),
            cornerRadius = cornerRadius,
            alpha = alpha
        )
    }
}
