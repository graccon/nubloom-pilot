package com.sujin.nubloompilot.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

private data class ShiftEvent(
    val startAbsoluteHour: Float,
    val endAbsoluteHour: Float,
    val color: Color,
    val label: String
)

private data class ShiftTimelineSegment(
    val startHour: Float,
    val endHour: Float,
    val color: Color,
    val label: String
)

fun DrawScope.drawTimelineSpiralLayer(
    layout: TimelineLayout,
    spiralConfig: TimelineSpiralConfig,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    currentTime: LocalTime
) {
    val basePath = createSpiralPath(
        layout = layout,
        config = spiralConfig
    )

    drawPath(
        path = basePath,
        color = Color(0xFF3A3A3A),
        style = Stroke(
            width = 106f,
            cap = StrokeCap.Round
        )
    )

    val shiftEvents = buildShiftEventQueue(
        yesterdayShift = yesterdayShift,
        todayShift = todayShift,
        tomorrowShift = tomorrowShift,
        dayAfterTomorrowShift = dayAfterTomorrowShift
    )

    val shiftSegments = shiftEvents
        .filterVisibleEvents(
            startAnchor = spiralConfig.startAnchor
        )
        .mapToTimelineSegments(
            startAnchor = spiralConfig.startAnchor
        )

    shiftSegments.forEach { segment ->
        val path = createSpiralSegmentPath(
            layout = layout,
            config = spiralConfig,
            startHour = segment.startHour,
            endHour = segment.endHour
        )

        drawPath(
            path = path,
            color = segment.color,
            style = Stroke(
                width = 82f,
                cap = StrokeCap.Round
            )
        )
    }

    val currentHour = currentTime.hour + currentTime.minute / 60f

    val currentTimelineHour = toTimelineHour(
        hour = currentHour,
        startAnchor = spiralConfig.startAnchor
    )

    drawCurrentTimeMarker(
        layout = layout,
        config = spiralConfig,
        hour = currentTimelineHour,
        color = Color(0xFFFF5A1F)
    )
}

private fun buildShiftEventQueue(
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?
): List<ShiftEvent> {
    return buildList {
        addAll(createShiftEvents(yesterdayShift, dayOffset = -24f))
        addAll(createShiftEvents(todayShift, dayOffset = 0f))
        addAll(createShiftEvents(tomorrowShift, dayOffset = 24f))
        addAll(createShiftEvents(dayAfterTomorrowShift, dayOffset = 48f))
    }
}

private fun createShiftEvents(
    shift: String?,
    dayOffset: Float
): List<ShiftEvent> {
    val color = when (shift) {
        "D" -> Color(0xFFA9C9EA)
        "E" -> Color(0xFFF4A249)
        "N" -> Color(0xFFEAB0D6)
        else -> return emptyList()
    }

    val startEnd = when (shift) {
        "D" -> dayOffset + 6.5f to dayOffset + 15.5f
        "E" -> dayOffset + 14.5f to dayOffset + 23.5f
        "N" -> dayOffset + 22.5f to dayOffset + 31.5f
        else -> return emptyList()
    }

    return listOf(
        ShiftEvent(
            startAbsoluteHour = startEnd.first,
            endAbsoluteHour = startEnd.second,
            color = color,
            label = shift
        )
    )
}

private fun List<ShiftEvent>.filterVisibleEvents(
    startAnchor: TimelineStartAnchor
): List<ShiftEvent> {
    val windowStart = startAnchor.startHour
    val windowEnd = windowStart + 48f

    return filter { event ->
        event.endAbsoluteHour > windowStart &&
                event.startAbsoluteHour < windowEnd
    }
}

private fun List<ShiftEvent>.mapToTimelineSegments(
    startAnchor: TimelineStartAnchor
): List<ShiftTimelineSegment> {
    val windowStart = startAnchor.startHour
    val windowEnd = windowStart + 48f

    return mapNotNull { event ->
        val visibleStart = maxOf(event.startAbsoluteHour, windowStart)
        val visibleEnd = minOf(event.endAbsoluteHour, windowEnd)

        if (visibleStart >= visibleEnd) {
            null
        } else {
            ShiftTimelineSegment(
                startHour = visibleStart - windowStart,
                endHour = visibleEnd - windowStart,
                color = event.color,
                label = event.label
            )
        }
    }
}

private fun DrawScope.drawCurrentTimeMarker(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    hour: Float,
    color: Color
) {
    val progress = hour / 48f

    val angleDegrees =
        config.startAngleDegrees + progress * config.totalAngleDegrees

    val angleRad = Math.toRadians(angleDegrees.toDouble())

    val spiralRadius =
        layout.outerRadius - progress * (layout.outerRadius - layout.innerRadius)

    val spiralPoint = Offset(
        x = layout.spiralCenter.x + cos(angleRad).toFloat() * spiralRadius,
        y = layout.spiralCenter.y + sin(angleRad).toFloat() * spiralRadius
    )

    val outerPoint = Offset(
        x = layout.circleCenter.x + cos(angleRad).toFloat() * (layout.clockRadius + 6.dp.toPx()),
        y = layout.circleCenter.y + sin(angleRad).toFloat() * (layout.clockRadius + 6.dp.toPx())
    )

    drawLine(
        color = color,
        start = outerPoint,
        end = spiralPoint,
        strokeWidth = 8.dp.toPx(),
        cap = StrokeCap.Round
    )
}