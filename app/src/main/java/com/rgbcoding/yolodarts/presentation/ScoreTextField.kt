package com.rgbcoding.yolodarts.presentation

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.rgbcoding.yolodarts.MainViewModel

@Composable
fun ScoreTextField(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {

    var isError by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editingValue by remember { mutableStateOf("") }
    val lastScore = viewModel.lastScores.collectAsState().value.lastOrNull()?.toString() ?: "-1"

    TextField(
        value = if (isEditing) editingValue else lastScore,
        onValueChange = { newValue ->
            isEditing = true
            editingValue = newValue
            isError = newValue.toIntOrNull() == null
            if (!isError) {
                viewModel.overrideScore(newValue)
            }
        },
        label = {
            Text(
                "Last score:",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        singleLine = true,  // Ensures single line input
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (!isError) {
                    viewModel.submitScoreOverride()
                    isEditing = false
                }
            }
        ),
        isError = isError,
        modifier = modifier
    )
}

/*
maybe?
colors = TextFieldDefaults.textFieldColors(
containerColor = MaterialTheme.colorScheme.secondaryContainer,
cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
focusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer
)
*/