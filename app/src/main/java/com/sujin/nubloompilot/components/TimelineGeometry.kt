package com.sujin.nubloompilot.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class TimelineStartAnchor(
    val startHour: Float,
    val startAngleDegrees: Float
) {
    MIDNIGHT(
        startHour = 0f,
        startAngleDegrees = -90f
    ),
    MORNING(
        startHour = 6f,
        startAngleDegrees = 0f
    ),
    NOON(
        startHour = 12f,
        startAngleDegrees = 90f
    ),
    EVENING(
        startHour = 18f,
        startAngleDegrees = 180f
    )
}

data class TimelineLayout(
    val circleCenter: Offset,
    val spiralCenter: Offset,
    val clockRadius: Float,
    val outerRadius: Float,
    val innerRadius: Float
)

data class TimelineSpiralConfig(
    val startAngleDegrees: Float,
    val totalAngleDegrees: Float,
    val startAnchor: TimelineStartAnchor
)

fun DrawScope.createTimelineLayout(
    startAnchor: TimelineStartAnchor
): TimelineLayout {
    val circleCenter = center

    val spiralCenter = when (startAnchor) {
        TimelineStartAnchor.MIDNIGHT -> Offset(
            x = circleCenter.x - 4.dp.toPx(),
            y = circleCenter.y + 8.dp.toPx()
        )

        TimelineStartAnchor.MORNING -> Offset(
            x = circleCenter.x - 8.dp.toPx(),
            y = circleCenter.y - 4.dp.toPx(),
        )

        TimelineStartAnchor.NOON -> Offset(
            x = circleCenter.x + 4.dp.toPx(),
            y = circleCenter.y - 8.dp.toPx()
        )

        TimelineStartAnchor.EVENING -> Offset(
            x = circleCenter.x + 8.dp.toPx(),
            y = circleCenter.y + 4.dp.toPx()
        )
    }

    return TimelineLayout(
        circleCenter = circleCenter,
        spiralCenter = spiralCenter,
        clockRadius = size.minDimension * 0.46f,
        outerRadius = size.minDimension * 0.38f,
        innerRadius = size.minDimension * 0.09f
    )
}

fun resolveTimelineStartAnchor(
    currentHour: Float
): TimelineStartAnchor {
    return when {
        currentHour < 6f -> TimelineStartAnchor.MIDNIGHT    // MIDNIGHT
        currentHour < 12f -> TimelineStartAnchor.MORNING    // MORNING
        currentHour < 18f -> TimelineStartAnchor.NOON       // NOON
        else -> TimelineStartAnchor.EVENING                 // EVENING
    }
}

fun createSpiralConfig(
    startAnchor: TimelineStartAnchor
): TimelineSpiralConfig {
    return TimelineSpiralConfig(
        startAngleDegrees = startAnchor.startAngleDegrees,
        totalAngleDegrees = 720f,
        startAnchor = startAnchor
    )
}

fun clockHourToAngle(hour: Float): Float {
    return (hour / 24f) * 360f - 90f
}

fun toTimelineHour(
    hour: Float,
    startAnchor: TimelineStartAnchor
): Float {
    return (hour - startAnchor.startHour + 48f) % 48f
}

fun createSpiralPath(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    steps: Int = 240
): Path {
    val path = Path()

    for (i in 0..steps) {
        val progress = i / steps.toFloat()
        val point = getSpiralPointByProgress(
            layout = layout,
            config = config,
            progress = progress
        )

        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    return path
}

fun createSpiralSegmentPath(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    startHour: Float,
    endHour: Float,
    totalHours: Float = 48f,
    steps: Int = 80
): Path {
    val path = Path()

    val startProgress = startHour / totalHours
    val endProgress = endHour / totalHours

    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val progress = startProgress + (endProgress - startProgress) * t

        val point = getSpiralPointByProgress(
            layout = layout,
            config = config,
            progress = progress
        )

        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    return path
}

fun getSpiralPoint(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    hour: Float,
    totalHours: Float = 48f
): Offset {
    val progress = hour / totalHours

    return getSpiralPointByProgress(
        layout = layout,
        config = config,
        progress = progress
    )
}

private fun getSpiralPointByProgress(
    layout: TimelineLayout,
    config: TimelineSpiralConfig,
    progress: Float
): Offset {
    val angleDegrees =
        config.startAngleDegrees + progress * config.totalAngleDegrees

    val angleRad = Math.toRadians(angleDegrees.toDouble())

    val radius =
        layout.outerRadius - progress * (layout.outerRadius - layout.innerRadius)

    return Offset(
        x = layout.spiralCenter.x + cos(angleRad).toFloat() * radius,
        y = layout.spiralCenter.y + sin(angleRad).toFloat() * radius
    )
}
