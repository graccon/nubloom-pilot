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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.models.ShiftType
import com.sujin.nubloompilot.models.TimelineMarker
import com.sujin.nubloompilot.models.getTimeRange
import com.sujin.nubloompilot.ui.theme.*
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

private data class TimelineSpiralStyle(
    val baseSpiralColor: Color,
    val baseSpiralStrokeWidth: Float,
    val shiftArcStrokeWidth: Float,
    val shiftLabelFontSize: TextUnit,
    val currentTimeMarkerOuterOffset: Float,
    val currentTimeMarkerInnerOffset: Float,
    val currentTimeMarkerStrokeWidth: Float,
    val markerBaseOuterSize: Float,
    val markerBaseInnerSize: Float
)

private fun DrawScope.getTimelineSpiralStyle(isWatchMode: Boolean): TimelineSpiralStyle {
    return if (isWatchMode) {
        TimelineSpiralStyle(
            baseSpiralColor = Gray300,
            baseSpiralStrokeWidth = 68f,
            shiftArcStrokeWidth = 46f,
            shiftLabelFontSize = 16.sp,
            currentTimeMarkerOuterOffset = 8.dp.toPx(),
            currentTimeMarkerInnerOffset = 34.dp.toPx(),
            currentTimeMarkerStrokeWidth = 8.dp.toPx(),
            markerBaseOuterSize = 24.dp.toPx(),
            markerBaseInnerSize = 32.dp.toPx()
        )
    } else {
        TimelineSpiralStyle(
            baseSpiralColor = Color(0xFF3A3A3A),
            baseSpiralStrokeWidth = 106f,
            shiftArcStrokeWidth = 68f,
            shiftLabelFontSize = 20.sp,
            currentTimeMarkerOuterOffset = 8.dp.toPx(),
            currentTimeMarkerInnerOffset = 34.dp.toPx(),
            currentTimeMarkerStrokeWidth = 8.dp.toPx(),
            markerBaseOuterSize = 24.dp.toPx(),
            markerBaseInnerSize = 32.dp.toPx()
        )
    }
}

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
    highlightedMarkerId: String? = null,
    revealProgress: Float = 1f,
    isWatchMode: Boolean = false
) {
    val style = getTimelineSpiralStyle(isWatchMode)
    
    // 1. Background Spiral Reveal (0.00f -> 0.75f)
    val backgroundReveal = segmentProgress(revealProgress, 0.00f, 0.75f)

    val basePath = createSpiralPath(
        layout = layout,
        config = spiralConfig,
        revealProgress = backgroundReveal
    )

    drawPath(
        path = basePath,
        color = style.baseSpiralColor,
        style = Stroke(
            width = style.baseSpiralStrokeWidth,
            cap = StrokeCap.Round
        )
    )

    // 2. Shift Arc & Label Fade-in (0.70f -> 1.00f)
    val shiftRevealAlpha = segmentProgress(revealProgress, 0.70f, 1.00f)

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
            color = segment.color.copy(alpha = segment.color.alpha * shiftRevealAlpha),
            style = Stroke(
                width = style.shiftArcStrokeWidth,
                cap = StrokeCap.Round
            )
        )

        drawShiftStartLabel(
            layout = layout,
            config = spiralConfig,
            segment = segment,
            textMeasurer = textMeasurer,
            revealAlpha = shiftRevealAlpha,
            fontSize = style.shiftLabelFontSize
        )
    }

    // Draw Intervention Markers
    val windowStart = spiralConfig.startAnchor.startHour
    val windowEnd = windowStart + 48f
    val isAnyHighlighted = highlightedMarkerId != null
    val markerRevealProgress = segmentProgress(revealProgress, 0.88f, 1.00f)

    // 1. Draw normal markers first
    markers.forEachIndexed { index, marker ->
        if (marker.id != highlightedMarkerId && marker.absoluteHour in windowStart..windowEnd) {
            val delay = index * 0.08f
            val localProgress = ((markerRevealProgress - delay) / 0.25f).coerceIn(0f, 1f)

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
                alpha = if (isAnyHighlighted) 0.3f else 1.0f,
                localProgress = localProgress,
                baseOuterSize = style.markerBaseOuterSize,
                baseInnerSize = style.markerBaseInnerSize
            )
        }
    }

    // 2. Draw highlighted marker last to be on top
    highlightedMarkerId?.let { id ->
        val index = markers.indexOfFirst { it.id == id }
        markers.find { it.id == id }?.let { marker ->
            if (marker.absoluteHour in windowStart..windowEnd) {
                val delay = if (index != -1) index * 0.08f else 0f
                val localProgress = ((markerRevealProgress - delay) / 0.25f).coerceIn(0f, 1f)

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
                    scale = 1.2f, // Highlight scale
                    localProgress = localProgress,
                    baseOuterSize = style.markerBaseOuterSize,
                    baseInnerSize = style.markerBaseInnerSize
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
            color = Color(0xFFFF5A1F),
            outerOffset = style.currentTimeMarkerOuterOffset,
            innerOffset = style.currentTimeMarkerInnerOffset,
            strokeWidth = style.currentTimeMarkerStrokeWidth
        )
    }
}

private fun DrawScope.drawInterventionMarker(
    position: Offset,
    marker: TimelineMarker,
    icon: ImageBitmap?,
    scale: Float = 1.0f,
    alpha: Float = 1.0f,
    localProgress: Float = 1f,
    baseOuterSize: Float,
    baseInnerSize: Float
) {
    if (localProgress <= 0f) return

    val popScale = 0.7f + (0.3f * localProgress)
    val finalScale = scale * popScale
    val finalAlpha = alpha * localProgress

    val outerSize = baseOuterSize * finalScale
    val innerSize = baseInnerSize * finalScale

    if (icon != null) {
        // Draw PNG Icon
        val topLeft = position - Offset(innerSize / 2, innerSize / 2)
        drawImage(
            image = icon,
            dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
            dstSize = IntSize(innerSize.toInt(), innerSize.toInt()),
            alpha = finalAlpha
        )
    } else {
        // Fallback to colored dot if icon is missing
        drawCircle(
            color = marker.color.copy(alpha = finalAlpha),
            radius = (innerSize / 2.5f) * finalScale,
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
    color: Color,
    outerOffset: Float,
    innerOffset: Float,
    strokeWidth: Float
) {
    val progress = hour / 48f

    val angleDegrees =
        config.startAngleDegrees + progress * config.totalAngleDegrees

    val angleRad = Math.toRadians(angleDegrees.toDouble())

    val outerPoint = Offset(
        x = layout.circleCenter.x + cos(angleRad).toFloat() * (layout.clockRadius + outerOffset),
        y = layout.circleCenter.y + sin(angleRad).toFloat() * (layout.clockRadius + outerOffset)
    )

    val innerPoint = Offset(
        x = layout.circleCenter.x + cos(angleRad).toFloat() * (layout.clockRadius - innerOffset),
        y = layout.circleCenter.y + sin(angleRad).toFloat() * (layout.clockRadius - innerOffset)
    )

    drawLine(
        color = color,
        start = outerPoint,
        end = innerPoint,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawShiftStartLabel(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    segment: ShiftTimelineSegment,
    textMeasurer: TextMeasurer,
    revealAlpha: Float = 1f,
    fontSize: TextUnit
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
        color = Gray900.copy(alpha = Gray900.alpha * revealAlpha),
        fontSize = fontSize,
        fontWeight = FontWeight.Black
    )
}
