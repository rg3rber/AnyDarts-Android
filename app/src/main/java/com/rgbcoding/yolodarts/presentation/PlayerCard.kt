package com.rgbcoding.yolodarts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.rgbcoding.yolodarts.data.Player

@Composable
fun PlayerCard(
    player: Player,
    isItsTurn: Boolean,
    modifier: Modifier
) {
    // allow changing the name on the fly
    val isEditingName = remember { mutableStateOf(false) }
    val editingNameText = remember { mutableStateOf(player.name.value) }

    // remove cursor once done
    val focusManager = LocalFocusManager.current

    val backgroundColor = if (isItsTurn) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    // Determine text color based on current player status
    val textColor = if (isItsTurn) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Column(
        modifier = modifier.background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = editingNameText.value,
            onValueChange = { newName ->
                editingNameText.value = newName
                player.nameChange(newName)
            },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = textColor,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .background(backgroundColor)
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    player.nameChange(editingNameText.value)
                    focusManager.clearFocus()
                }
            ),
        )

        Text(
            player.scoreLeft.value.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = textColor
        )
        Text(
            if (player.throws.value.isEmpty()) "-" else player.throws.value.last().toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
        Text(
            if (player.throws.value.isEmpty()) "-" else player.throws.value.average().toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )

    }
}


@Preview
@Composable
fun previewPlayerCard() {
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
