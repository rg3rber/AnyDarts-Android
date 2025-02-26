package com.rgbcoding.yolodarts.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rgbcoding.yolodarts.ui.theme.backgroundLight

@Composable
fun BoardSquare(
    modifier: Modifier,
) {
    val contentColor = backgroundLight
    val strokeWidth = 4f
    val cornerRadius = 20f
    val extensionLength: Float = 40f
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(screenWidth / 8)
    ) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            // Top-left corner
            moveTo(cornerRadius + extensionLength, 0f)
            lineTo(cornerRadius, 0f)
            arcTo(
                rect = Rect(
                    offset = Offset(0f, 0f),
                    size = Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = true
            )
            moveTo(0f, cornerRadius)
            lineTo(0f, cornerRadius + extensionLength)  // Vertical extension

            // Top-right corner
            moveTo(width - (cornerRadius + extensionLength), 0f)  // Start with horizontal extension
            lineTo(width - cornerRadius, 0f)
            arcTo(
                rect = Rect(
                    offset = Offset(width - cornerRadius * 2, 0f),
                    size = Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = true
            )
            lineTo(width, cornerRadius + extensionLength)  // Vertical extension

            // Bottom-left corner
            moveTo(0f, height - (cornerRadius + extensionLength))  // Start with vertical extension
            lineTo(0f, height - cornerRadius)  // Draw to start of arc
            arcTo(
                rect = Rect(
                    offset = Offset(0f, height - cornerRadius * 2),
                    size = Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = true
            )
            moveTo(cornerRadius, height)
            lineTo(cornerRadius + extensionLength, height)  // Horizontal extension

            // Bottom-right corner
            moveTo(
                width,
                height - (cornerRadius + extensionLength)
            )  // Start with vertical extension
            lineTo(width, height - cornerRadius)  // Draw to start of arc
            arcTo(
                rect = Rect(
                    offset = Offset(width - cornerRadius * 2, height - cornerRadius * 2),
                    size = Size(cornerRadius * 2, cornerRadius * 2)
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = true
            )
            lineTo(width - (cornerRadius + extensionLength), height)  // Horizontal extension
        }

        drawPath(
            path = path,
            color = contentColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}

// Usage example:
@Preview
@Composable
fun PreviewBoardSquare() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp)
    ) {
        BoardSquare(
            modifier = Modifier.fillMaxSize(),
        )
    }
}