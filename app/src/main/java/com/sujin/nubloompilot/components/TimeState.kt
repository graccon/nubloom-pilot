package com.sujin.nubloompilot.components

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.LocalTime

@Composable
fun rememberCurrentTime(): LocalTime {
    var currentTime by remember {
        mutableStateOf(LocalTime.now())
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()

            val now = LocalTime.now()
            val delayMillis =
                ((60 - now.second) * 1000L).coerceAtLeast(1000L)

            delay(delayMillis)
        }
    }

    return currentTime
}