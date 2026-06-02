package com.sujin.nubloompilot.shared.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.shared.models.TimelineMarker
import com.sujin.nubloompilot.shared.models.ShiftTimingConfig
import com.sujin.nubloompilot.shared.components.*
import com.sujin.nubloompilot.shared.ui.theme.Gray700
import com.sujin.nubloompilot.shared.ui.theme.Gray800
import java.time.LocalTime
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SpiralTimeline(
    yesterdayShift: String? = null,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String? = null,
    currentTime: LocalTime = LocalTime.now(),
    markers: List<TimelineMarker> = emptyList(),
    markerIcons: Map<Int, ImageBitmap> = emptyMap(),
    highlightedMarkerId: String? = null,
    revealProgress: Float = 1f,
    modifier: Modifier = Modifier,
    isWatchMode: Boolean = false,
    shiftTimingConfig: ShiftTimingConfig = ShiftTimingConfig.Default
) {
    val textMeasurer = rememberTextMeasurer()
    
    // 1. Long press state to store coordinates
    var pressedOffset by remember { mutableStateOf<Offset?>(null) }
    var isPressed by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf<Float?>(null) }
    var selectedTimeText by remember { mutableStateOf<String?>(null) }

    val timelineScale by animateFloatAsState(
        targetValue = if (isPressed) 1.08f else 1f,
        label = "timelineScale"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (!isWatchMode) {
            TimelineSelectionHeader(
                isPressed = isPressed,
                selectedTimeText = selectedTimeText
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = timelineScale
                    scaleY = timelineScale
                }
                .pointerInput(Unit) {
                    // 2. Gesture detection
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            // 3. Store initial press coordinates
                            isPressed = true
                            pressedOffset = offset
                        },
                        onDrag = { change, _ ->
                            // 4. Update coordinates during drag
                            pressedOffset = change.position
                        },
                        onDragEnd = {
                            // 6. Keep the last value for debugging (do not clear)
                            isPressed = false
                        },
                        onDragCancel = {
                            // 7. Clear only on cancel
                            isPressed = false
                            pressedOffset = null
                            selectedHour = null
                            selectedTimeText = null
                        }
                    )
                }
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
                currentHour = currentHour,
                revealProgress = revealProgress,
                isWatchMode = isWatchMode
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
                markerIcons = markerIcons,
                showCurrentTimeIndicator = !isPressed,
                highlightedMarkerId = highlightedMarkerId,
                revealProgress = revealProgress,
                isWatchMode = isWatchMode,
                shiftTimingConfig = shiftTimingConfig
            )

            // Step 5: Draw a focus point at the nearest location on the spiral (Visible only when pressed)
            if (isPressed) {
                pressedOffset?.let { touchOffset ->
                    val snapResult = findNearestPointOnSpiral(touchOffset, layout, spiralConfig)
                    selectedHour = snapResult.hour
                    selectedTimeText = formatSelectedTimelineTime(
                        selectedHour = snapResult.hour,
                        startAnchor = startAnchor.startHour
                    )
                    
                    drawCircle(
                        color = Color(0xFFFF5A1F),
                        radius = 8.dp.toPx(),
                        center = snapResult.point
                    )
                }
            }
        }
    }
}

private data class SpiralSnapResult(
    val point: Offset,
    val hour: Float
)

/**
 * Finds the nearest point on the spiral path based on a raw touch coordinate.
 * Uses a sampling approach to find the minimum distance.
 */
private fun findNearestPointOnSpiral(
    touchOffset: Offset,
    layout: TimelineLayout,
    config: TimelineSpiralConfig
): SpiralSnapResult {
    var minDistance = Float.MAX_VALUE
    var nearestPoint = Offset.Zero
    var nearestHour = 0f

    // Sample the spiral at 0.1h intervals (480 points)
    var currentHour = 0f
    while (currentHour <= 48f) {
        val spiralPoint = getSpiralPoint(layout, config, currentHour)
        val dx = touchOffset.x - spiralPoint.x
        val dy = touchOffset.y - spiralPoint.y
        val distanceSquared = dx * dx + dy * dy // Optimization: compare squared distance

        if (distanceSquared < minDistance) {
            minDistance = distanceSquared
            nearestPoint = spiralPoint
            nearestHour = currentHour
        }
        currentHour += 0.1f
    }
    return SpiralSnapResult(
        point = nearestPoint,
        hour = nearestHour
    )
}

private fun formatSelectedTimelineTime(
    selectedHour: Float,
    startAnchor: Float
): String {
    val totalHour = startAnchor + selectedHour
    
    val dateLabel = when {
        totalHour < 24f -> "오늘"
        totalHour < 48f -> "내일"
        else -> "모레"
    }

    val normalizedHour = ((totalHour % 24f) + 24f) % 24f
    val hour = normalizedHour.toInt()
    val minute = ((normalizedHour - hour) * 60).toInt()

    return "$dateLabel %02d:%02d".format(hour, minute)
}

@Composable
private fun TimelineSelectionHeader(
    isPressed: Boolean,
    selectedTimeText: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth() // 0.85f
            .height(36.dp),
        // TODO 위치 재조정 - 롱프레스시에 겹침
        contentAlignment = Alignment.BottomStart
    ) {
        if (isPressed) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(
                            Color(0xFFFF5A1F),
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = selectedTimeText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
            }
        }
    }
}
