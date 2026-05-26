package com.sujin.nubloompilot.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.models.ShiftType
import com.sujin.nubloompilot.models.TimelineMarker
import com.sujin.nubloompilot.models.getTimeRange
import com.sujin.nubloompilot.ui.theme.Gray900
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
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
    currentTime: LocalTime,
    textMeasurer: TextMeasurer,
    markers: List<TimelineMarker> = emptyList(),
    markerIcons: Map<Int, ImageBitmap> = emptyMap(),
    showCurrentTimeIndicator: Boolean = true,
    highlightedMarkerId: String? = null
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

    val today = LocalDate.now()
    val shiftEvents = buildShiftEventQueue(
        referenceDate = today,
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

        drawShiftStartLabel(
            layout = layout,
            config = spiralConfig,
            segment = segment,
            textMeasurer = textMeasurer
        )
    }

    // Draw Intervention Markers
    val windowStart = spiralConfig.startAnchor.startHour
    val windowEnd = windowStart + 48f
    val isAnyHighlighted = highlightedMarkerId != null

    // 1. Draw normal markers first
    markers.forEach { marker ->
        if (marker.id != highlightedMarkerId && marker.absoluteHour in windowStart..windowEnd) {
            val timelineHour = marker.absoluteHour - windowStart
            val position = getSpiralPoint(
                layout = layout,
                config = spiralConfig,
                hour = timelineHour
            )

            drawInterventionMarker(
                position = position,
                marker = marker,
                icon = markerIcons[marker.iconRes],
                alpha = if (isAnyHighlighted) 0.3f else 1.0f
            )
        }
    }

    // 2. Draw highlighted marker last to be on top
    highlightedMarkerId?.let { id ->
        markers.find { it.id == id }?.let { marker ->
            if (marker.absoluteHour in windowStart..windowEnd) {
                val timelineHour = marker.absoluteHour - windowStart
                val position = getSpiralPoint(
                    layout = layout,
                    config = spiralConfig,
                    hour = timelineHour
                )

                drawInterventionMarker(
                    position = position,
                    marker = marker,
                    icon = markerIcons[marker.iconRes],
                    scale = 1.2f // Highlight scale
                )
            }
        }
    }

    val currentHour = currentTime.hour + currentTime.minute / 60f

    val currentTimelineHour = toTimelineHour(
        hour = currentHour,
        startAnchor = spiralConfig.startAnchor
    )

    if (showCurrentTimeIndicator) {
        drawCurrentTimeMarker(
            layout = layout,
            config = spiralConfig,
            hour = currentTimelineHour,
            color = Color(0xFFFF5A1F)
        )
    }
}

private fun DrawScope.drawInterventionMarker(
    position: Offset,
    marker: TimelineMarker,
    icon: ImageBitmap?,
    scale: Float = 1.0f,
    alpha: Float = 1.0f
) {
    val baseOuterSize = 24.dp.toPx()
    val baseInnerSize = 32.dp.toPx()
    
    val outerSize = baseOuterSize * scale
    val innerSize = baseInnerSize * scale

    if (icon != null) {
        // Draw PNG Icon
        val topLeft = position - Offset(innerSize / 2, innerSize / 2)
        drawImage(
            image = icon,
            dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
            dstSize = IntSize(innerSize.toInt(), innerSize.toInt()),
            alpha = alpha
        )
    } else {
        // Fallback to colored dot if icon is missing
        drawCircle(
            color = marker.color.copy(alpha = alpha),
            radius = (innerSize / 2.5f) * scale,
            center = position
        )
    }
}

private fun buildShiftEventQueue(
    referenceDate: LocalDate,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?
): List<ShiftEvent> {
    return buildList {
        addAll(createShiftEvents(ShiftType.fromString(yesterdayShift), referenceDate.minusDays(1), referenceDate))
        addAll(createShiftEvents(ShiftType.fromString(todayShift), referenceDate, referenceDate))
        addAll(createShiftEvents(ShiftType.fromString(tomorrowShift), referenceDate.plusDays(1), referenceDate))
        addAll(createShiftEvents(ShiftType.fromString(dayAfterTomorrowShift), referenceDate.plusDays(2), referenceDate))
    }
}

private fun createShiftEvents(
    shiftType: ShiftType,
    date: LocalDate,
    referenceDate: LocalDate
): List<ShiftEvent> {
    if (shiftType == ShiftType.OFF) return emptyList()

    val timeRange = shiftType.getTimeRange(date)
    val startTime = timeRange.startTime ?: return emptyList()
    val endTime = timeRange.endTime ?: return emptyList()

    return listOf(
        ShiftEvent(
            startAbsoluteHour = startTime.toAbsoluteHour(referenceDate),
            endAbsoluteHour = endTime.toAbsoluteHour(referenceDate),
            color = shiftType.color,
            label = shiftType.label
        )
    )
}

private fun LocalDateTime.toAbsoluteHour(referenceDate: LocalDate): Float {
    val daysBetween = ChronoUnit.DAYS.between(referenceDate, this.toLocalDate())
    return (daysBetween * 24f) + this.hour + this.minute / 60f
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

    val outerPoint = Offset(
        x = layout.circleCenter.x + cos(angleRad).toFloat() * (layout.clockRadius + 8.dp.toPx()),
        y = layout.circleCenter.y + sin(angleRad).toFloat() * (layout.clockRadius + 8.dp.toPx())
    )

    val innerPoint = Offset(
        x = layout.circleCenter.x + cos(angleRad).toFloat() * (layout.clockRadius - 34.dp.toPx()),
        y = layout.circleCenter.y + sin(angleRad).toFloat() * (layout.clockRadius - 34.dp.toPx())
    )

    drawLine(
        color = color,
        start = outerPoint,
        end = innerPoint,
        strokeWidth = 8.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawShiftStartLabel(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    segment: ShiftTimelineSegment,
    textMeasurer: TextMeasurer
) {
    val segmentDuration = segment.endHour - segment.startHour

    if (segmentDuration < 1.5f) {
        return
    }

    val labelHour = segment.startHour + 0.1f

    val labelPosition = getSpiralPoint(
        layout = layout,
        config = config,
        hour = labelHour
    )

    drawCenteredText(
        textMeasurer = textMeasurer,
        text = segment.label,
        position = labelPosition,
        color = Gray900,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black
    )
}
