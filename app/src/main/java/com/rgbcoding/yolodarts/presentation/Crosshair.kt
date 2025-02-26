package com.rgbcoding.yolodarts.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rgbcoding.yolodarts.ui.theme.backgroundLight


@Composable
fun Crosshair(
    modifier: Modifier
) {
    val contentColor = backgroundLight
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val tenthWidth = canvasWidth / 16
        // horizontal
        drawLine(
            start = Offset(x = canvasWidth / 2 - tenthWidth, y = canvasHeight / 2),
            end = Offset(x = canvasWidth / 2 + tenthWidth, y = canvasHeight / 2),
            color = contentColor,
            strokeWidth = 4.0f
        )
        // vertical
        drawLine(
            start = Offset(x = canvasWidth / 2, y = canvasHeight / 2 - tenthWidth),
            end = Offset(x = canvasWidth / 2, y = canvasHeight / 2 + tenthWidth),
            color = contentColor,
            strokeWidth = 4.0f
        )

    }
}

@Preview
@Composable
fun previewCrosshair() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Crosshair(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
        )
    }
}