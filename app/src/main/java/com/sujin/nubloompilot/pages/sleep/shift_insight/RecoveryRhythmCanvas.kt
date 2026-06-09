package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.models.RecoveryRhythmGraphData
import com.sujin.nubloompilot.models.RecoverySleepWindow
import com.sujin.nubloompilot.models.ShiftInsightType
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray500
import androidx.compose.ui.graphics.Color

@Composable
fun RecoveryRhythmCanvas(
    graphData: RecoveryRhythmGraphData,
    visibleShifts: Set<ShiftInsightType>,
    focusedShift: ShiftInsightType,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    // Bold styles
    val baseStrokeWidth = with(density) { 14.dp.toPx() }
    val highlightStrokeWidth = with(density) { 9.dp.toPx() }
    val labelFontSize = with(density) { 10.sp.toPx() }
    
    // Dummy sleep windows if not provided
    val displayedSleepWindows = remember(graphData.sleepWindows, focusedShift) {
        if (graphData.sleepWindows.isNotEmpty()) graphData.sleepWindows
        else {
            val windows = mutableListOf<RecoverySleepWindow>()
            // Always show OFF window
            windows.add(RecoverySleepWindow("Off 기준 수면", 0.67f, 8.5f, ShiftInsightType.OFF))
            // If focused is not OFF, add focused window
            if (focusedShift != ShiftInsightType.OFF) {
                val (start, end) = when(focusedShift) {
                    ShiftInsightType.DAY -> 23.5f to 8.17f
                    ShiftInsightType.EVENING -> 2.5f to 10.5f
                    ShiftInsightType.NIGHT -> 9.5f to 15.5f
                    else -> 0f to 0f
                }
                windows.add(0, RecoverySleepWindow("${focusedShift.name} 평균 수면", start, end, focusedShift))
            }
            windows
        }
    }

    Canvas(modifier = modifier) {
        val plotLeft = 48.dp.toPx()
        val plotRight = size.width - 24.dp.toPx()
        val plotTop = 16.dp.toPx()
        
        // Split canvas: Top for rhythm (approx 180dp), Bottom for sleep lanes
        val rhythmAreaHeight = 180.dp.toPx()
        val plotBottom = plotTop + rhythmAreaHeight
        
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        // --- 1. Draw Axis / Grids / Labels ---
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = labelFontSize
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        
        drawContext.canvas.nativeCanvas.drawText("높음", plotLeft - 8.dp.toPx(), plotTop + labelFontSize/2, textPaint)
        drawContext.canvas.nativeCanvas.drawText("낮음", plotLeft - 8.dp.toPx(), plotBottom, textPaint)

        drawLine(
            color = Gray500,
            start = Offset(plotLeft, plotBottom),
            end = Offset(plotRight, plotBottom),
            strokeWidth = 1.dp.toPx()
        )
        
        val xLabels = listOf(0, 6, 12, 18, 24)
        xLabels.forEach { hour ->
            val x = plotLeft + (hour / 24f) * plotWidth
            drawContext.canvas.nativeCanvas.drawText(
                "${hour}h", 
                x, 
                plotBottom + 16.dp.toPx(), 
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = labelFontSize
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        val gridLevels = listOf(0.25f, 0.5f, 0.75f,  1.0f)
        gridLevels.forEach { level ->
            val y = plotBottom - (level * plotHeight)
            drawLine(
                color = Gray400,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        // --- 2. Draw Series Lines (High Density Line Segments) ---
        graphData.series.filter { visibleShifts.contains(it.shiftType) }.forEach { series ->
            if (series.points.size < 2) return@forEach
            
            val color = getSeriesColor(series.shiftType)
            val coords = series.points.map { point ->
                Offset(
                    plotLeft + (point.hour / 24f) * plotWidth,
                    plotBottom - (point.level * plotHeight)
                )
            }

            // 2-a. Draw Base Faded Line (Entire Path)
            val fullPath = Path().apply {
                moveTo(coords[0].x, coords[0].y)
                coords.drop(1).forEach { lineTo(it.x, it.y) }
            }
            
            drawPath(
                path = fullPath,
                color = color.copy(alpha = 0.2f),
                style = Stroke(
                    width = baseStrokeWidth,
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // 2-b. Draw Highlighted Segments (Work Time Only)
            for (i in 0 until series.points.size - 1) {
                val p1 = series.points[i]
                val p2 = series.points[i+1]
                
                if (p1.isWorkTime && p2.isWorkTime) {
                    // Continuous line within work time
                    drawLine(
                        color = color,
                        start = coords[i],
                        end = coords[i+1],
                        strokeWidth = highlightStrokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // --- 3. Draw Sleep Window Bars ---
        val sleepLaneTopBase = plotBottom + 42.dp.toPx()
        val sleepLaneHeight = 12.dp.toPx()
        val sleepLaneGap = 16.dp.toPx()
        
        displayedSleepWindows.forEachIndexed { index, window ->
            val laneY = sleepLaneTopBase + index * (sleepLaneHeight + sleepLaneGap)
            val barColor = window.colorType?.let { getSeriesColor(it) } ?: Color.DarkGray
            val isFocused = window.colorType == focusedShift
            
            drawSleepWindowBar(
                startHour = window.startHour,
                endHour = window.endHour,
                y = laneY,
                height = sleepLaneHeight,
                plotLeft = plotLeft,
                plotWidth = plotWidth,
                color = barColor,
                alpha = if (isFocused) 0.4f else 0.2f
            )
            
            // Draw label and time
            val labelText = "${window.label} ${formatHourToTimeLabel(window.startHour)} - ${formatHourToTimeLabel(window.endHour)}"
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                plotLeft,
                laneY - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = labelFontSize * 0.9f
                    textAlign = android.graphics.Paint.Align.LEFT
                    isFakeBoldText = isFocused
                }
            )
        }
    }
}
