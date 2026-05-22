package com.sujin.nubloompilot.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400

class SpeechBubbleShape(
    private val cornerRadius: Dp = 20.dp,
    private val tailWidth: Dp = 16.dp,
    private val tailHeight: Dp = 20.dp,
    private val tailOffsetFromBottom: Dp = 26.dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        with(density) {
            val radius = cornerRadius.toPx()
            val tailW = tailWidth.toPx()
            val tailH = tailHeight.toPx()
            val tailOffset = tailOffsetFromBottom.toPx()

            val tailTop = size.height - tailOffset - tailH
            val tailBottom = tailTop + tailH

            val path = Path().apply {
                moveTo(radius, 0f)

                lineTo(size.width - radius, 0f)
                quadraticTo(size.width, 0f, size.width, radius)

                lineTo(size.width, size.height - radius)
                quadraticTo(
                    size.width,
                    size.height,
                    size.width - radius,
                    size.height
                )

                lineTo(radius, size.height)
                quadraticTo(0f, size.height, 0f, size.height - radius)

                lineTo(0f, tailBottom)
                lineTo(-tailW, tailBottom)
                lineTo(0f, tailTop)

                lineTo(0f, radius)
                quadraticTo(0f, 0f, radius, 0f)
            }

            return Outline.Generic(path)
        }
    }
}

@Composable
fun SpeechBubbleCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Gray300,
    contentPadding: PaddingValues = PaddingValues(
        start = 20.dp,
        top = 14.dp,
        end = 20.dp,
        bottom = 28.dp
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .background(
                color = backgroundColor,
                shape = SpeechBubbleShape()
            )
            .padding(contentPadding),
        content = content
    )
}