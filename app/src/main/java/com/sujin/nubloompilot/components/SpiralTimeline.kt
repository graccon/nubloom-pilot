package com.sujin.nubloompilot.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Primary

import kotlin.math.cos
import kotlin.math.sin


@Composable
fun SpiralTimeline(
    todayShift: String?,
    tomorrowShift: String?,
    startPeriod: TimelineStartPeriod = TimelineStartPeriod.AM,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier.size(320.dp)
    ) {
        val center = this.center
        val radius = size.minDimension * 0.46f

        val dayColor = Color(0xFFB8B5B5)
        val outlineColor = Color(0xFF3A3A3A)
        val textColor = Color(0xFF2F2F2F)

        val outerRadius = size.minDimension * 0.38f
        val innerRadius = size.minDimension * 0.09f


        val spiralStartAngle = when (startPeriod) {
            TimelineStartPeriod.AM -> -90f
            TimelineStartPeriod.PM -> 90f
        }



        val spiralPath = createSpiralPath(
            center = center,
            outerRadius = outerRadius,
            innerRadius = innerRadius,
            startAngleDegrees = spiralStartAngle,
            totalAngleDegrees = 720f
        )
        val spiralPathA = createSpiralPath(
            center = center,
            outerRadius = outerRadius,
            innerRadius = innerRadius,
            startAngleDegrees = spiralStartAngle,
            totalAngleDegrees = 720f
        )


        drawNightArea(
            center = center,
            radius = radius,
            color = dayColor
        )

        drawCircle(
            color = outlineColor,
            radius = radius,
            center = center,
            style = Stroke(width = 16f)
        )

        drawDashedVerticalLine(
            center = center,
            radius = radius,
            color = Gray700
        )

        drawPath(
            path = spiralPath,
            color = outlineColor,
            style = Stroke(
                width = 106f,
                cap = StrokeCap.Round
            )
        )


        val shiftSegments = buildShiftSegments(
            todayShift = todayShift,
            tomorrowShift = tomorrowShift
        )

        shiftSegments.forEach { segment ->
            val segmentPath = createSpiralSegmentPath(
                center = center,
                outerRadius = outerRadius,
                innerRadius = innerRadius,
                startAngleDegrees = spiralStartAngle,
                totalAngleDegrees = 720f,
                segmentStartHour = segment.startHour,
                segmentEndHour = segment.endHour
            )

            val labelPosition = getSpiralPoint(
                center = center,
                outerRadius = outerRadius,
                innerRadius = innerRadius,
                startAngleDegrees = spiralStartAngle,
                totalAngleDegrees = 720f,
                hour = segment.startHour
            )

            drawPath(
                path = segmentPath,
                color = segment.color,
                style = Stroke(
                    width = 82f,
                    cap = StrokeCap.Round
                )
            )

            drawCenteredText(
                textMeasurer = textMeasurer,
                text = segment.label,
                position = labelPosition,
                color = Gray800,
                fontSize = 22.sp
            )
        }

        val timeMarks = listOf(
            TimelineTimeMark(hour = 0f, label = "12AM"),
            TimelineTimeMark(hour = 6f, label = "6"),
            TimelineTimeMark(hour = 12f, label = "12PM"),
            TimelineTimeMark(hour = 18f, label = "6")
        )

        timeMarks.forEach { mark ->
            val angle = clockHourToAngle(mark.hour)

            drawTick(
                center = center,
                radius = radius,
                angleDegrees = angle,
                color = outlineColor
            )

            drawTimelineLabel(
                textMeasurer = textMeasurer,
                center = center,
                radius = radius,
                angleDegrees = angle,
                text = mark.label,
                color = textColor,
                fontSize = if (mark.label == "6") 24.sp else 22.sp
            )
        }
    }
}

private data class ShiftTimelineSegment(
    val startHour: Float,
    val endHour: Float,
    val color: Color,
    val label: String
)

private fun buildShiftSegments(
    todayShift: String?,
    tomorrowShift: String?
): List<ShiftTimelineSegment> {
    return buildList {
        addAll(
            shiftToSegments(
                shift = todayShift,
                dayOffset = 0f
            )
        )

        addAll(
            shiftToSegments(
                shift = tomorrowShift,
                dayOffset = 24f
            )
        )
    }
}

private fun shiftToSegments(
    shift: String?,
    dayOffset: Float
): List<ShiftTimelineSegment> {
    val color = when (shift) {
        "D" -> Color(0xFFA9C9EA)
        "E" -> Color(0xFFF4A249)
        "N" -> Color(0xFFEAB0D6)
        else -> return emptyList()
    }

    val rawSegment = when (shift) {
        "D" -> dayOffset + 6.5f to dayOffset + 15.5f
        "E" -> dayOffset + 14.5f to dayOffset + 23.5f
        "N" -> dayOffset + 22.5f to dayOffset + 31.5f
        else -> return emptyList()
    }

    val start = rawSegment.first.coerceIn(0f, 48f)
    val end = rawSegment.second.coerceIn(0f, 48f)

    if (start >= end) return emptyList()

    return listOf(
        ShiftTimelineSegment(
            startHour = start,
            endHour = end,
            color = color,
            label = shift
        )
    )
}

private data class TimelineTimeMark(
    val hour: Float,
    val label: String
)

private fun hourToAngle(
    hour: Float,
    startPeriod: TimelineStartPeriod
): Float {
    val normalizedHour = (hour - startPeriod.startHour + 24f) % 24f
    return (normalizedHour / 24f) * 360f - 90f
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDashedVerticalLine(
    center: Offset,
    radius: Float,
    color: Color
) {
    val dashHeight = 2.dp.toPx()
    val gapHeight = 12.dp.toPx()
    val strokeWidth = 5.dp.toPx()

    var y = center.y - radius + 10.dp.toPx()
    val endY = center.y + radius - 8.dp.toPx()

    while (y < endY) {
        drawLine(
            color = color,
            start = Offset(center.x, y),
            end = Offset(center.x, (y + dashHeight).coerceAtMost(endY)),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        y += dashHeight + gapHeight
    }
}

private fun clockHourToAngle(hour: Float): Float {
    return (hour / 24f) * 360f - 90f
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNightArea(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x - radius, center.y)

        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = center.x - radius,
                top = center.y - radius,
                right = center.x + radius,
                bottom = center.y + radius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        lineTo(center.x + radius, center.y)
        lineTo(center.x - radius, center.y)
        close()
    }

    drawPath(
        path = path,
        color = color
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTick(
    center: Offset,
    radius: Float,
    angleDegrees: Float,
    color: Color
) {
    val angleRad = Math.toRadians(angleDegrees.toDouble())

    val innerRadius = radius + 2.dp.toPx()
    val outerRadius = radius + 8.dp.toPx()

    val start = Offset(
        x = center.x + cos(angleRad).toFloat() * innerRadius,
        y = center.y + sin(angleRad).toFloat() * innerRadius
    )

    val end = Offset(
        x = center.x + cos(angleRad).toFloat() * outerRadius,
        y = center.y + sin(angleRad).toFloat() * outerRadius
    )

    drawLine(
        color = color,
        start = start,
        end = end,
        cap = StrokeCap.Round,
        strokeWidth = 16f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTimelineLabel(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    center: Offset,
    radius: Float,
    angleDegrees: Float,
    text: String,
    color: Color,
    fontSize: TextUnit
) {
    val angleRad = Math.toRadians(angleDegrees.toDouble())
    val labelRadius = radius + 28.dp.toPx()

    val position = Offset(
        x = center.x + cos(angleRad).toFloat() * labelRadius,
        y = center.y + sin(angleRad).toFloat() * labelRadius
    )

    drawCenteredText(
        textMeasurer = textMeasurer,
        text = text,
        position = position,
        color = color,
        fontSize = fontSize
    )
}




private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCenteredText(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    position: Offset,
    color: Color,
    fontSize: TextUnit
) {
    val result = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.Black
        )
    )

    drawText(
        textLayoutResult = result,
        topLeft = Offset(
            x = position.x - result.size.width / 2f,
            y = position.y - result.size.height / 2f
        )
    )
}

private fun createSpiralPath(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    startAngleDegrees: Float,
    totalAngleDegrees: Float,
    steps: Int = 240
): Path {
    val path = Path()

    for (i in 0..steps) {
        val progress = i / steps.toFloat()
        val angleDegrees = startAngleDegrees + progress * totalAngleDegrees
        val angleRad = Math.toRadians(angleDegrees.toDouble())

        val radius = outerRadius - progress * (outerRadius - innerRadius)

        val point = Offset(
            x = center.x + cos(angleRad).toFloat() * radius,
            y = center.y + sin(angleRad).toFloat() * radius
        )

        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    return path
}

private fun createSpiralSegmentPath(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    startAngleDegrees: Float,
    totalAngleDegrees: Float,
    segmentStartHour: Float,
    segmentEndHour: Float,
    totalHours: Float = 48f,
    steps: Int = 80
): Path {
    val path = Path()

    val startProgress = segmentStartHour / totalHours
    val endProgress = segmentEndHour / totalHours

    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val progress = startProgress + (endProgress - startProgress) * t

        val angleDegrees = startAngleDegrees + progress * totalAngleDegrees
        val angleRad = Math.toRadians(angleDegrees.toDouble())

        val radius = outerRadius - progress * (outerRadius - innerRadius)

        val point = Offset(
            x = center.x + cos(angleRad).toFloat() * radius,
            y = center.y + sin(angleRad).toFloat() * radius
        )

        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    return path
}

private fun getSpiralPoint(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    startAngleDegrees: Float,
    totalAngleDegrees: Float,
    hour: Float,
    totalHours: Float = 48f
): Offset {
    val progress = hour / totalHours
    val angleDegrees = startAngleDegrees + progress * totalAngleDegrees
    val angleRad = Math.toRadians(angleDegrees.toDouble())

    val radius = outerRadius - progress * (outerRadius - innerRadius)

    return Offset(
        x = center.x + cos(angleRad).toFloat() * radius,
        y = center.y + sin(angleRad).toFloat() * radius
    )
}