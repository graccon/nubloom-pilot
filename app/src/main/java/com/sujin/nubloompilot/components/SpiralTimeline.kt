package com.sujin.nubloompilot.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@Composable
fun SpiralTimeline(
    yesterdayShift: String? = null,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String? = null,
    currentTime: LocalTime = LocalTime.now(),
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier.size(300.dp)
    ) {
        val currentHour = currentTime.hour + currentTime.minute / 60f

        val startAnchor = resolveTimelineStartAnchor(currentHour)

        val layout = createTimelineLayout(
            startAnchor = startAnchor
        )

        val spiralConfig = createSpiralConfig(
            startAnchor = startAnchor
        )

        drawTimelineBackground(
            layout = layout,
            textMeasurer = textMeasurer,
            currentHour = currentHour
        )

        drawTimelineSpiralLayer(
            layout = layout,
            spiralConfig = spiralConfig,
            yesterdayShift = yesterdayShift,
            todayShift = todayShift,
            tomorrowShift = tomorrowShift,
            dayAfterTomorrowShift = dayAfterTomorrowShift,
            currentTime = currentTime
        )
    }
}