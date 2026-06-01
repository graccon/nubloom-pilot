package com.sujin.nubloompilot.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.wear.compose.material.MaterialTheme
import com.sujin.nubloompilot.shared.components.SpiralTimeline
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var timelineInfo by remember {
        mutableStateOf(WearTimelineLocalStore(context).getTimelineInfo())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                timelineInfo = WearTimelineLocalStore(context).getTimelineInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            SpiralTimeline(
                yesterdayShift = timelineInfo.yesterdayShift ?: "N",
                todayShift = timelineInfo.todayShift ?: "D",
                tomorrowShift = timelineInfo.tomorrowShift ?: "D",
                dayAfterTomorrowShift = timelineInfo.dayAfterTomorrowShift ?: "OFF",
                currentTime = LocalTime.now(),
                isWatchMode = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }
    }
}
