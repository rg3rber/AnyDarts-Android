package com.rgbcoding.yolodarts.presentation

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rgbcoding.yolodarts.MainViewModel
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.takeAndUploadPhoto

@Composable
fun ScoreTextField(
    viewModel: MainViewModel,
    isAutoScoringMode: Boolean,
    controller: LifecycleCameraController,
    context: Context,
    modifier: Modifier = Modifier
) {

    //this does not work: also in general you have too much logic inside Views (composables here the textfield) here you should just observe flows and conditionally
    // display different things or call different functions then the viewmodel does all the logic part...
    // val currentUploadState by viewModel.uploadState.collectAsState()

    var textFieldValue by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // State from ViewModel
    val lastScore by viewModel.lastScore.collectAsState()
    val currentUploadState by viewModel.uploadState.collectAsState()
    val scoreValidationError by viewModel.scoreValidationError.collectAsState()

    // Alerts
    val openAlertDialog = remember { mutableStateOf(false) }
    val alertCode = remember { mutableStateOf<AlertCode?>(null) }

    // Focus management
    val focusManager = LocalFocusManager.current

    // Update textField when ViewModel's lastScore changes (if not editing)
    LaunchedEffect(lastScore) {
        if (!isEditing) {
            // If lastScore is null, clear the field
            textFieldValue = lastScore?.toString() ?: ""
        }
    }

    // TODO is this needed?
    LaunchedEffect(currentUploadState) {
        if (currentUploadState is UploadState.Success) {
            val score = (currentUploadState as UploadState.Success).score
            textFieldValue = score.toString()
            // Use the new function to update the viewModel
            viewModel.updateLastScore(score)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp), // Rounded only at the top
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Go Back One Turn",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    isEditing = true
                    textFieldValue = newValue
                    viewModel.validateScoreInTextfield(newValue)
                },
                label = {
                    Text(
                        "Total score",
                        style = MaterialTheme.typography.labelSmall,
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
                        val submitResult = submitScore(viewModel, textFieldValue, alertCode, openAlertDialog)
                        if (submitResult == AlertCode.VALID_SCORE) {
                            textFieldValue = "" // Clear the field after successful submission
                        }
                        focusManager.clearFocus()
                        isEditing = false
                    }
                ),
                isError = scoreValidationError != null,
                modifier = Modifier
                    .weight(2f)
                    .padding(horizontal = 4.dp),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,  // Background when focused
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface, // Background when not focused
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceBright, // Background when disabled
                )

            )
            //Submit / Get Button
            Button(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(MaterialTheme.colorScheme.background)
                    .weight(1f),
                onClick = {
                    Log.d("Scoring", "Clicked on Get/Submit Button")
                    // manual mode:
                    if (!isAutoScoringMode) {
                        val submitResult = submitScore(viewModel, textFieldValue, alertCode, openAlertDialog)
                        if (submitResult == AlertCode.VALID_SCORE) {
                            textFieldValue = "" // Clear the field after successful submission
                        }
                        isEditing = false
                        //focusManager.clearFocus()
                    } else { // Auto mode:
                        val currentState = viewModel.uploadState.value
                        if (currentState is UploadState.Success) {
                            val submitResult = submitScore(viewModel, textFieldValue, alertCode, openAlertDialog)
                            if (submitResult == AlertCode.VALID_SCORE) {
                                textFieldValue = "" // Clear the field after successful submission
                            }
                            isEditing = false
                        } else if (currentState !is UploadState.Uploading) {
                            if (viewModel.isDebugMode.value) {
                                viewModel.dummyGetScore(viewModel, UploadState.Success(77), 77)
                            } else {
                                takeAndUploadPhoto(
                                    controller = controller,
                                    context = context,
                                    viewModel = viewModel
                                )
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Submitted Score of: ${(viewModel.uploadState.value as UploadState.Success).score} is invalid",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    focusManager.clearFocus() // always clear focus
                },
                colors = buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = if (!isAutoScoringMode || (isAutoScoringMode && currentUploadState is UploadState.Success)) "Submit\nScore" else "Get\nScore",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (openAlertDialog.value) {
            GameAlertDialog(
                onDismissRequest = {
                    if (alertCode.value == AlertCode.GAME_OVER) {
                        viewModel.endGame()
                        openAlertDialog.value = false
                    } else {
                        openAlertDialog.value = false
                    }
                },
                onConfirmation = { openAlertDialog.value = false },
                dialogTitle = alertCode.toString(),
                dialogText = "This is an example of an alert dialog with buttons.",
                icon = Icons.Default.Info,
                alertCode = alertCode.value
            )
        }
    }
}

private fun submitScore(
    viewModel: MainViewModel,
    scoreText: String,
    alertCode: MutableState<AlertCode?>,
    openAlertDialog: MutableState<Boolean>
): AlertCode {
    val result = viewModel.submitScore(scoreText)
    alertCode.value = result
    if (result != AlertCode.VALID_SCORE) {
        openAlertDialog.value = true
    }
    return result
}


@Preview(showBackground = true)
@Composable
fun PreviewScoreTextField() {
    // Create a mock ViewModel TODO fix this similar to the app bar
    val mockViewModel = MainViewModel(application = Application()) // Ensure you have a no-arg constructor or use MockK

    // Use a dummy context (will not actually work in preview but avoids compilation errors)
    val dummyContext = android.content.ContextWrapper(null)

    // Use a fake LifecycleCameraController (it won't function, but compiles)
    val fakeController = LifecycleCameraController(dummyContext)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ScoreTextField(
            viewModel = mockViewModel,
            isAutoScoringMode = false,
            controller = fakeController,
            context = dummyContext,
            modifier = Modifier.fillMaxSize()
        )
    }
}
