package com.sujin.nubloompilot.components


import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@Composable
fun SpriteAnimation(
    frames: List<Int>,
    modifier: Modifier = Modifier,
    frameDuration: Long = 200L
) {
    var currentFrame by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(frames) {
        while (true) {
            delay(frameDuration)
            currentFrame = (currentFrame + 1) % frames.size
        }
    }

    Image(
        painter = painterResource(id = frames[currentFrame]),
        contentDescription = null,
        modifier = modifier
    )
}