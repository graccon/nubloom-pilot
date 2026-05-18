package com.sujin.nubloompilot.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import kotlin.math.roundToInt

@Composable
fun FloatingIcon(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: Int = 60,
    tint: Color = Color.Unspecified,
    floatDistance: Float = -12f,
    durationMillis: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = floatDistance,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(size.dp)
            .offset {
                IntOffset(
                    x = 0,
                    y = floatOffset.roundToInt()
                )
            }
    )
}