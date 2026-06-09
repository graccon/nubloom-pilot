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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.models.RecoveryRhythmSeries
import com.sujin.nubloompilot.models.RecoveryRhythmSeriesPoint
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import androidx.compose.ui.graphics.PathEffect

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
        val middleY = plotBottom - (0.5f * plotHeight)
        drawContext.canvas.nativeCanvas.drawText(
            "보통",
            plotLeft - 8.dp.toPx(),
            middleY + labelFontSize / 2,
            textPaint
        )
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
        val xGridHours = listOf(6, 12, 18)

        xGridHours.forEach { hour ->
            val x = plotLeft + (hour / 24f) * plotWidth

            drawLine(
                color = Gray400.copy(alpha = 0.7f),
                start = Offset(x, plotTop),
                end = Offset(x, plotBottom),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(4f, 6f),
                    phase = 0f
                )
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
                color = color.copy(alpha = 0.25f),
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

    }
}

@Preview(
    name = "Recovery Rhythm Canvas - Day",
    showBackground = true,
    widthDp = 360,
    heightDp = 320
)
@Composable
private fun RecoveryRhythmCanvasPreview() {
    val dummyGraphData = RecoveryRhythmGraphData(
        title = "근무 유형별 예상 회복 리듬",
        description = "DAY 근무 시 예상되는 회복 리듬입니다.",
        series = listOf(
            RecoveryRhythmSeries(
                shiftType = ShiftInsightType.DAY,
                label = "Day 근무",
                points = listOf(
                    RecoveryRhythmSeriesPoint(0f, 0.25f, false),
                    RecoveryRhythmSeriesPoint(3f, 0.18f, false),
                    RecoveryRhythmSeriesPoint(6f, 0.42f, false),
                    RecoveryRhythmSeriesPoint(9f, 0.68f, true),
                    RecoveryRhythmSeriesPoint(12f, 0.75f, true),
                    RecoveryRhythmSeriesPoint(15f, 0.62f, true),
                    RecoveryRhythmSeriesPoint(18f, 0.45f, false),
                    RecoveryRhythmSeriesPoint(21f, 0.32f, false),
                    RecoveryRhythmSeriesPoint(24f, 0.26f, false)
                ),
                workStartHour = 6.5f,
                workEndHour = 15.5f
            ),
            RecoveryRhythmSeries(
                shiftType = ShiftInsightType.OFF,
                label = "Off 기준",
                points = listOf(
                    RecoveryRhythmSeriesPoint(0f, 0.35f, false),
                    RecoveryRhythmSeriesPoint(3f, 0.22f, false),
                    RecoveryRhythmSeriesPoint(6f, 0.30f, false),
                    RecoveryRhythmSeriesPoint(9f, 0.58f, false),
                    RecoveryRhythmSeriesPoint(12f, 0.80f, false),
                    RecoveryRhythmSeriesPoint(15f, 0.75f, false),
                    RecoveryRhythmSeriesPoint(18f, 0.60f, false),
                    RecoveryRhythmSeriesPoint(21f, 0.42f, false),
                    RecoveryRhythmSeriesPoint(24f, 0.34f, false)
                ),
                workStartHour = null,
                workEndHour = null
            )
        ),
        sleepWindows = listOf(
            RecoverySleepWindow(
                label = "Day 평균 수면",
                startHour = 23.5f,
                endHour = 8.17f,
                colorType = ShiftInsightType.DAY
            ),
            RecoverySleepWindow(
                label = "Off 기준 수면",
                startHour = 0.67f,
                endHour = 8.5f,
                colorType = ShiftInsightType.OFF
            )
        )
    )

    NubloomPilotTheme {
        RecoveryRhythmCanvas(
            graphData = dummyGraphData,
            visibleShifts = setOf(
                ShiftInsightType.DAY,
                ShiftInsightType.OFF
            ),
            focusedShift = ShiftInsightType.DAY,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color.White)
        )
    }
}