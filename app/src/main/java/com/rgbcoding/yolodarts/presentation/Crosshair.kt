package com.rgbcoding.yolodarts.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color


@Composable
fun Crosshair(
    modifier: Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val tenthWidth = canvasWidth / 10
        // horizontal
        drawLine(
            start = Offset(x = canvasWidth / 2 - tenthWidth, y = canvasHeight / 2),
            end = Offset(x = canvasWidth / 2 + tenthWidth, y = canvasHeight / 2),
            color = Color.Blue,
            strokeWidth = 2.0f
        )
        // vertical
        drawLine(
            start = Offset(x = canvasWidth / 2, y = canvasHeight / 2 - tenthWidth),
            end = Offset(x = canvasWidth / 2, y = canvasHeight / 2 + tenthWidth),
            color = Color.Blue,
            strokeWidth = 2.0f
        )

    }
}