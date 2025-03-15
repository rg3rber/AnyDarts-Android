package com.rgbcoding.yolodarts.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    isItsTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val isEditingName = remember { mutableStateOf(false) }
    val editingNameText = remember { mutableStateOf(player.name.value) }
    val focusManager = LocalFocusManager.current

    val backgroundColor = if (isItsTurn) {
        MaterialTheme.colorScheme.surfaceBright
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val textColor = if (isItsTurn) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val cornerRadius = 16.dp
    val colorAlpha = if (isItsTurn) 0.3f else 0.6f

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .clip(shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp))

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            BasicTextField(
                value = editingNameText.value,
                onValueChange = { newName ->
                    editingNameText.value = newName
                    player.nameChange(newName)
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        player.nameChange(editingNameText.value)
                        focusManager.clearFocus()
                    }
                ),
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "SCORE LEFT",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = textColor,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        player.scoreLeft.value.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                        color = textColor,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "LAST SCORE",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = textColor,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        if (player.throws.value.isEmpty()) "-" else player.throws.value.last().toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp),
                        color = textColor,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "3-DART AVG",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = textColor,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        if (player.throws.value.isEmpty()) "-" else formatAverage(player),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 24.sp),
                        color = textColor,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatAverage(player: Player): String {
    val average = player.throws.value.average()
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
            PlayerCard(player1, false, Modifier.weight(1f))
            PlayerCard(player2, true, Modifier.weight(1f))
        }
    }
}