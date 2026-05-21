package com.sujin.nubloompilot.components


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.ui.theme.Gray700
import kotlin.math.cos
import kotlin.math.sin

private data class TimelineTimeMark(
    val hour: Float,
    val label: String
)

fun DrawScope.drawTimelineBackground(
    layout: TimelineLayout,
    textMeasurer: TextMeasurer,
    currentHour: Float
) {
    val nightColor = Color(0xFFB8B5B5)
    val outlineColor = Color(0xFF3A3A3A)
    val textColor = Color(0xFF2F2F2F)

    drawNightArea(
        center = layout.circleCenter,
        radius = layout.clockRadius,
        color = nightColor
    )

    drawCircle(
        color = outlineColor,
        radius = layout.clockRadius,
        center = layout.circleCenter,
        style = Stroke(width = 16f)
    )

    drawDashedVerticalLine(
        center = layout.circleCenter,
        radius = layout.clockRadius,
        color = Gray700
    )

    val timeMarks = listOf(
        TimelineTimeMark(hour = 0f, label = "12AM"),
        TimelineTimeMark(hour = 6f, label = "6"),
        TimelineTimeMark(hour = 12f, label = "12PM"),
        TimelineTimeMark(hour = 18f, label = "6")
    )

    timeMarks.forEach { mark ->
        val angle = clockHourToAngle(mark.hour)

        drawTick(
            center = layout.circleCenter,
            radius = layout.clockRadius,
            angleDegrees = angle,
            color = textColor
        )

        drawTimelineLabel(
            textMeasurer = textMeasurer,
            center = layout.circleCenter,
            radius = layout.clockRadius,
            angleDegrees = angle,
            text = mark.label,
            color = textColor,
            fontSize = 18.sp
        )
    }

    drawMinorTimeMarks(
        layout = layout,
        textMeasurer = textMeasurer,
        color = outlineColor,
        textColor = textColor,
        currentHour = currentHour
    )


}

private fun DrawScope.drawNightArea(
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

private fun DrawScope.drawDashedVerticalLine(
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

private fun DrawScope.drawTick(
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

private fun DrawScope.drawTimelineLabel(
    textMeasurer: TextMeasurer,
    center: Offset,
    radius: Float,
    angleDegrees: Float,
    text: String,
    color: Color,
    fontSize: TextUnit
) {
    val angleRad = Math.toRadians(angleDegrees.toDouble())
    val labelRadius = radius + 24.dp.toPx()

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

fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    position: Offset,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.SemiBold
){
    val result = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
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

private fun DrawScope.drawMinorTimeMarks(
    layout: TimelineLayout,
    textMeasurer: TextMeasurer,
    color: Color,
    textColor: Color,
    currentHour: Float
) {
    val activeRange = when {
        currentHour < 6f -> 0 until 6
        currentHour < 12f -> 6 until 12
        currentHour < 18f -> 12 until 18
        else -> 18 until 24
    }

    val majorHours = setOf(0, 6, 12, 18)

    val labelMap = mapOf(
        2 to "2",
        4 to "4",
        8 to "8",
        10 to "10",
        14 to "2",
        16 to "4",
        20 to "8",
        22 to "10"
    )

    val minorHours = activeRange
        .filter { hour -> hour !in majorHours }

    minorHours.forEach { hour ->
        val angle = clockHourToAngle(hour.toFloat())
        val angleRad = Math.toRadians(angle.toDouble())

        val dotRadius = layout.clockRadius + 8.dp.toPx()
        val labelRadius = layout.clockRadius + 24.dp.toPx()

        val dotPosition = Offset(
            x = layout.circleCenter.x + cos(angleRad).toFloat() * dotRadius,
            y = layout.circleCenter.y + sin(angleRad).toFloat() * dotRadius
        )

        drawCircle(
            color = color.copy(alpha = 0.55f),
            radius = (3.5).dp.toPx(),
            center = dotPosition
        )

        val label = labelMap[hour]

        if (label != null) {
            val labelPosition = Offset(
                x = layout.circleCenter.x + cos(angleRad).toFloat() * labelRadius,
                y = layout.circleCenter.y + sin(angleRad).toFloat() * labelRadius
            )

            drawCenteredText(
                textMeasurer = textMeasurer,
                text = label,
                position = labelPosition,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}