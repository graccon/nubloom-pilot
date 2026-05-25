package com.sujin.nubloompilot.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import com.sujin.nubloompilot.models.TimelineMarker
import java.time.LocalTime

@Composable
fun SpiralTimeline(
    yesterdayShift: String? = null,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String? = null,
    currentTime: LocalTime = LocalTime.now(),
    markers: List<TimelineMarker> = emptyList(),
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    
    // Load PNG icons safely into ImageBitmap.
    // Vectors will be null here and fall back to dots in the drawing layer.
    val markerIcons = remember(markers) {
        val map = mutableMapOf<Int, ImageBitmap>()
        markers.forEach { marker ->
            val resId = marker.iconRes
            if (!map.containsKey(resId)) {
                runCatching {
                    BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
                }.getOrNull()?.let {
                    map[resId] = it
                }
            }
        }
        map
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(1f)
    ){
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
            currentTime = currentTime,
            textMeasurer = textMeasurer,
            markers = markers,
            markerIcons = markerIcons
        )
    }
}
