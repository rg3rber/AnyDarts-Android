package com.rgbcoding.yolodarts.presentation

import android.content.Context
import android.widget.Toast
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rgbcoding.yolodarts.MainViewModel
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.takeAndUploadPhoto

@Composable
fun ScoreTextField(
    viewModel: MainViewModel,
    isAutoScoringMode: Boolean,
    currentUploadState: UploadState,
    controller: LifecycleCameraController,
    context: Context,
    modifier: Modifier = Modifier
) {
    var isError by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editingValue by remember { mutableStateOf("") }
    val lastScore = viewModel.lastScores.collectAsState().value.lastOrNull()?.toString() ?: ""

    //alerts
    val openAlertDialog = remember { mutableStateOf(false) }
    val alertCode = remember { mutableStateOf<AlertCode?>(null) }

    // remove cursor once done
    val focusManager = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { viewModel.goBack() },
            modifier = Modifier.weight(0.5f),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Go Back One Turn",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        TextField(
            value = if (isEditing) editingValue else lastScore,
            onValueChange = { newValue ->
                isEditing = true
                editingValue = newValue
                isError = viewModel.overrideScore(newValue)
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
                        //viewModel.submitScoreOverride()
                        focusManager.clearFocus()
                    }
                }
            ),
            isError = isError,
            modifier = Modifier
                .weight(2f)
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp)
        )
        Button(
            modifier = Modifier
                .padding(start = 4.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
                .weight(1f),
            onClick = {
                if (!isAutoScoringMode) {
                    alertCode.value = viewModel.submitScoreOverride()
                    if (alertCode.value != AlertCode.VALID_SCORE) {
                        openAlertDialog.value = true
                    }
                } else {
                    if (currentUploadState is UploadState.Success) {
                        //TODO this makes no sense?? if i overrrode the get score my overridden score will be overrriden?
                        if (viewModel.overrideScore(currentUploadState.score.toString())) {
                            alertCode.value = viewModel.submitScoreOverride()
                            if (alertCode.value != AlertCode.VALID_SCORE) {
                                // something with the score went wrong => show dialog
                                openAlertDialog.value = true
                                viewModel.setUploadState(UploadState.Idle)
                            } else {
                                viewModel.setUploadState(UploadState.Idle)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Submitted Score of: ${currentUploadState.score} is invalid",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    if (currentUploadState !is UploadState.Uploading) {
                        takeAndUploadPhoto(
                            controller = controller,
                            context = context,
                            viewModel = viewModel
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Waiting for previous upload to finish",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                isEditing = false // after submitting reset editing
            },
            colors = buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                if (!isAutoScoringMode || (isAutoScoringMode && currentUploadState is UploadState.Success)) "Submit Score" else "Get Score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    if (openAlertDialog.value) {
        GameAlertDialog(
            onDismissRequest = { openAlertDialog.value = false },
            onConfirmation = { openAlertDialog.value = false },
            dialogTitle = alertCode.toString(),
            dialogText = "This is an example of an alert dialog with buttons.",
            icon = Icons.Default.Info,
            alertCode = alertCode.value
        )
    }
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