package com.rgbcoding.yolodarts.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rgbcoding.yolodarts.R
import com.rgbcoding.yolodarts.data.Player

@Composable
fun PlayerCard(
    player: Player,
    modifier: Modifier = Modifier
) {
    val playerName by player.name.collectAsState()
    val playerScoreLeft by player.scoreLeft.collectAsState()
    val playerThrows by player.throws.collectAsState()

    val isEditingName = remember { mutableStateOf(false) }
    val editingNameText = remember { mutableStateOf(player.name.value) }
    val focusManager = LocalFocusManager.current

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val textColor = MaterialTheme.colorScheme.onSurface

    val cornerRadius = 16.dp
    val colorAlpha = 0.6f

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = cornerRadius, bottomEnd = cornerRadius))
    ) {
        Image(
            painter = painterResource(id = R.drawable.yd_bg_3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = colorAlpha))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = 0.4f)
                        ),
                        center = Offset(0.5f, 0.5f),
                        radius = 0.2f // Adjust this value to control the size of the soft border
                    )
                )
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            BasicTextField(
                value = editingNameText.value,
                onValueChange = { newName ->
                    editingNameText.value = newName
                    player.nameChange(newName)
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = textColor,
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(IntrinsicSize.Min),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        player.nameChange(editingNameText.value)
                        focusManager.clearFocus()
                    }
                ),
            )
            Text(
                playerScoreLeft.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                color = textColor,
            )
            Text(
                if (playerThrows.isEmpty()) "-" else playerThrows.last().toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = textColor,
            )
            Text(
                if (playerThrows.isEmpty()) "-" else formatAverage(playerThrows),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = textColor,
            )
            Text(
                if (playerThrows.isEmpty()) "0" else (playerThrows.size * 3).toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = textColor,
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatAverage(playerThrows: List<Int>): String {
    val average = playerThrows.average()
    return String.format("%.2f", average)
}

@Preview
@Composable
fun PreviewPlayerCard() {
    val player1 = Player("Player 1")
    val player2 = Player("Player 2")
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PlayerCard(player1, Modifier.weight(1f))
            PlayerCard(player2, Modifier.weight(1f))
        }
    }
}