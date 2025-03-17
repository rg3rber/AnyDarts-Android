package com.rgbcoding.yolodarts.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rgbcoding.yolodarts.R
import com.rgbcoding.yolodarts.data.Player

@Composable
fun ExtensivePlayerCard(
    player: Player,
    isItsTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val playerName by player.name.collectAsState()
    val playerScoreLeft by player.scoreLeft.collectAsState()
    val playerThrows by player.throws.collectAsState()

    val isEditingName = remember { mutableStateOf(false) }
    val editingNameText = remember { mutableStateOf(player.name.value) }
    val focusManager = LocalFocusManager.current

    var backgroundColor = MaterialTheme.colorScheme.surfaceBright
    var topCornerRadius: Dp = 0.dp
    var underlineColor = MaterialTheme.colorScheme.tertiary

    if (!isItsTurn) {
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer
        topCornerRadius = 16.dp
        underlineColor = Color.Transparent
    }
    val textColor = MaterialTheme.colorScheme.onBackground

    val cornerRadius = 16.dp
    val colorAlpha = 0.3f

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(shape = RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius, bottomStart = cornerRadius, bottomEnd = cornerRadius))

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
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
            ) {

                UnderlinedPlayerName(editingNameText, player, textColor, underlineColor, focusManager)
                Text(
                    playerScoreLeft.toString(),
                    modifier = Modifier
                        .border(border = BorderStroke(2.dp, color = underlineColor), shape = RoundedCornerShape(8.dp))
                        .background(color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 34.sp),
                    color = textColor,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "LAST SCORE",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = textColor,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    if (playerThrows.isEmpty()) "-" else playerThrows.last().toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    color = textColor,
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
                    if (playerThrows.isEmpty()) "-" else formatAverage(playerThrows),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    color = textColor,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "DARTS THROWN",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = textColor,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    if (playerThrows.isEmpty()) "0" else (playerThrows.size * 3).toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun UnderlinedPlayerName(
    editingNameText: MutableState<String>,
    player: Player,
    textColor: Color,
    underlineColor: Color,
    focusManager: FocusManager
) {
    var textFieldWidth by remember { mutableIntStateOf(0) } // Stores the width
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
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
                fontSize = 24.sp,
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .padding(start = 16.dp)
                .width(IntrinsicSize.Min)
                .onGloballyPositioned { coordinates ->
                    textFieldWidth = coordinates.size.width + 30// Capture width here!
                },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    player.nameChange(editingNameText.value)
                    focusManager.clearFocus()
                }
            ),
        )
        Canvas(
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldWidth.toDp() }) // Apply captured width as dp
                .padding(bottom = 2.dp, start = 16.dp)
        ) {
            //val textWidth = editingNameText.value.length * 28f
            val startX = 0f //(size.width - textWidth) / 2
            drawLine(
                color = underlineColor,
                start = Offset(startX, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 4f // Custom thickness
            )
        }
    }
}


@Preview
@Composable
fun PreviewExtensivePlayerCard() {
    val player1 = Player("Player 1")
    val player2 = Player("Player 2")
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ExtensivePlayerCard(player = player1, isItsTurn = true, modifier = Modifier.weight(1f))
            ExtensivePlayerCard(player = player2, isItsTurn = false, modifier = Modifier.weight(1f))
        }
    }
}