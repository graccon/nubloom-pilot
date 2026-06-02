package com.sujin.nubloompilot.wear.wearface

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.sujin.nubloompilot.shared.components.*
import com.sujin.nubloompilot.wear.WatchInterventionMarkerMapper
import com.sujin.nubloompilot.wear.WearTimelineLocalStore
import java.time.LocalTime
import java.time.ZonedDateTime

class SpiralWatchFaceService : WatchFaceService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("SpiralWatchFace", "SpiralWatchFaceService.onCreate()")
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        Log.d("SpiralWatchFace", "createWatchFace started")
        val renderer = SpiralCanvasRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.HARDWARE
        )
        Log.d("SpiralWatchFace", "Renderer created")

        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = renderer
        ).apply {
            Log.d("SpiralWatchFace", "WatchFace instance created")
        }
    }
}

class SpiralCanvasRenderer(
    private val context: android.content.Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = canvasType,
    interactiveDrawModeUpdateDelayMillis = 16L,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = true
) {
    private val store = WearTimelineLocalStore(context)
    private val drawScope = CanvasDrawScope()
    private val density = Density(context)
    private val layoutDirection = LayoutDirection.Ltr
    
    // Cache for marker icons to avoid repeated loading
    private val markerIcons = mutableMapOf<Int, ImageBitmap>()

    // Attempting to create a TextMeasurer for the non-Compose environment
    private val textMeasurer = try {
        Log.d("SpiralWatchFace", "Creating TextMeasurer...")
        TextMeasurer(
            defaultFontFamilyResolver = createFontFamilyResolver(context),
            defaultDensity = density,
            defaultLayoutDirection = layoutDirection
        ).also {
            Log.d("SpiralWatchFace", "TextMeasurer created successfully")
        }
    } catch (e: Exception) {
        Log.e("SpiralWatchFace", "Failed to create TextMeasurer", e)
        null
    }

    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        Log.v("SpiralWatchFace", "render() called at $zonedDateTime")
        // 1. Black background
        canvas.drawColor(android.graphics.Color.BLACK)

        // 2. Prepare Compose DrawScope bridge
        val composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)
        val size = Size(bounds.width().toFloat(), bounds.height().toFloat())

        // 3. Drawing parameters
        val currentTime = LocalTime.now()
        val currentHour = currentTime.hour + currentTime.minute / 60f
        val startAnchor = resolveTimelineStartAnchor(currentHour)
        val timelineInfo = store.getTimelineInfo()
        
        val markers = WatchInterventionMarkerMapper.map(
            interventions = timelineInfo.interventions,
            referenceDateString = timelineInfo.referenceDate
        )

        // Ensure icons are loaded
        markers.forEach { marker ->
            val resId = marker.iconRes
            if (resId != 0 && !markerIcons.containsKey(resId)) {
                runCatching {
                    BitmapFactory.decodeResource(context.resources, resId)
                        ?.asImageBitmap()
                }.getOrNull()?.let {
                    markerIcons[resId] = it
                }
            }
        }

        drawScope.draw(
            density = density,
            layoutDirection = layoutDirection,
            canvas = composeCanvas,
            size = size
        ) {
            val layout = createTimelineLayout(startAnchor)
            val spiralConfig = createSpiralConfig(startAnchor)

            // Draw Background
            textMeasurer?.let { tm ->
                drawTimelineBackground(
                    layout = layout,
                    textMeasurer = tm,
                    currentHour = currentHour,
                    isWatchMode = true
                )

                // Draw Spiral Layer (Real shifts & markers with icons)
                drawTimelineSpiralLayer(
                    layout = layout,
                    spiralConfig = spiralConfig,
                    yesterdayShift = timelineInfo.yesterdayShift ?: "D",
                    todayShift = timelineInfo.todayShift ?: "D",
                    tomorrowShift = timelineInfo.tomorrowShift ?: "D",
                    dayAfterTomorrowShift = timelineInfo.dayAfterTomorrowShift ?: "OFF",
                    currentTime = currentTime,
                    textMeasurer = tm,
                    markers = markers,
                    markerIcons = markerIcons,
                    isWatchMode = true
                )
            }
        }
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
    }
}
