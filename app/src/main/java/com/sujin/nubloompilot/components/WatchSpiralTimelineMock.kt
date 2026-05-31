package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.shared.components.SpiralTimeline
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.shared.models.TimelineMarker
import com.sujin.nubloompilot.shared.ui.theme.Primary
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import java.time.LocalDateTime
import android.graphics.BitmapFactory
import androidx.compose.runtime.remember

/**
 * A mock preview of the SpiralTimeline as it would appear on a Wear OS watch screen.
 * This is used to test scaling and information density before moving to a dedicated Wear module.
 */
@Composable
fun WatchSpiralTimelineMock(
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    currentTime: LocalDateTime,
    modifier: Modifier = Modifier,
    markers: List<TimelineMarker> = emptyList()
) {
    val context = LocalContext.current
    val markerIcons = remember(markers) {
        val map = mutableMapOf<Int, ImageBitmap>()
        markers.forEach { marker ->
            val resId = marker.iconRes
            if (!map.containsKey(resId)) {
                runCatching {
                    BitmapFactory.decodeResource(context.resources, resId)
                        ?.asImageBitmap()
                }.getOrNull()?.let {
                    map[resId] = it
                }
            }
        }
        map
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        SpiralTimeline(
            yesterdayShift = yesterdayShift,
            todayShift = todayShift,
            tomorrowShift = tomorrowShift,
            dayAfterTomorrowShift = dayAfterTomorrowShift,
            currentTime = currentTime.toLocalTime(),
            markers = markers,
            markerIcons = markerIcons,
            modifier = Modifier.padding(0.dp), // Adjust padding to fit within circle
            isWatchMode = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WatchSpiralTimelineMockPreview() {
    NubloomPilotTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            WatchSpiralTimelineMock(
                yesterdayShift = "N",
                todayShift = "D",
                tomorrowShift = "E",
                dayAfterTomorrowShift = "OFF",
                currentTime = LocalDateTime.now()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WatchSpiralTimelineMockWithMarkersPreview() {
    val now = LocalDateTime.now()
    val dummyMarkers = listOf(
        TimelineMarker(
            id = "m1",
            absoluteHour = (now.hour + 2) + now.minute / 60f,
            color = Primary,
            label = "카페인",
            iconRes = R.drawable.ic_coffee
        ),
        TimelineMarker(
            id = "m2",
            absoluteHour = (now.hour + 5) + now.minute / 60f,
            color = Color(0xFF3F51B5),
            label = "낮잠",
            iconRes = R.drawable.ic_sleep_face
        )
    )

    NubloomPilotTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            WatchSpiralTimelineMock(
                yesterdayShift = "N",
                todayShift = "D",
                tomorrowShift = "E",
                dayAfterTomorrowShift = "OFF",
                currentTime = now,
                markers = dummyMarkers
            )
        }
    }
}
