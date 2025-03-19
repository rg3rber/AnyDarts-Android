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
import com.rgbcoding.yolodarts.domain.toReadableString
import com.rgbcoding.yolodarts.takeAndUploadPhoto

@Composable
fun ScoreTextField(
    viewModel: MainViewModel,
    isAutoScoringMode: Boolean,
    controller: LifecycleCameraController,
    context: Context,
    modifier: Modifier = Modifier
) {
    var isError by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editingValue by remember { mutableStateOf("") }
    val lastScore = viewModel.lastScore.collectAsState().value ?: ""

    //this does not work: also in general you have too much logic inside Views (composables here the textfield) here you should just observe flows and conditionally
    // display different things or call different functions then the viewmodel does all the logic part...
    val currentUploadState by viewModel.uploadState.collectAsState()

    //alerts
    val openAlertDialog = remember { mutableStateOf(false) }
    val alertCode = remember { mutableStateOf<AlertCode?>(null) }

    // remove cursor once done
    val focusManager = LocalFocusManager.current

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
                value = if (isEditing) editingValue else lastScore.toString(),
                onValueChange = { newValue ->
                    isEditing = true
                    editingValue = newValue
                    isError = viewModel.overrideScore(newValue)
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
                        if (!isError) {
                            viewModel.submitScoreOverride()
                            focusManager.clearFocus()
                        }
                        isEditing = false
                    }
                ),
                isError = isError,
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
                    if (!isAutoScoringMode) {
                        val scoreInScoreField = viewModel.lastScore.value
                        isError = viewModel.overrideScore(scoreInScoreField.toString())
                        alertCode.value = viewModel.submitScoreOverride()
                        if (alertCode.value != AlertCode.VALID_SCORE) {
                            openAlertDialog.value = true
                        }
                    } else {
                        val currentState = viewModel.uploadState.value
                        if (currentState is UploadState.Success) {
                            //TODO this makes no sense?? if i overrrode the get score my overridden score will be overrriden?
                            if (!viewModel.overrideScore(viewModel.lastScore.value.toString())) {
                                //override did not cause an error
                                alertCode.value = viewModel.submitScoreOverride()
                                if (alertCode.value != AlertCode.VALID_SCORE) {
                                    // something with the score went wrong => show dialog
                                    openAlertDialog.value = true
                                    //viewModel.setUploadState(UploadState.Idle)
                                } else {
                                    // TODO if it worked do nothing?
                                    Log.d(
                                        "Scoring",
                                        "Sore overridden and submitted currentUploadSteate = ${viewModel.uploadState.value.toReadableString()} and viemodeluploadstate = ${viewModel.uploadState.value.toReadableString()}"
                                    )
                                    //viewModel.setUploadState(UploadState.Idle)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Submitted Score of: ${(viewModel.uploadState.value as UploadState.Success).score} is invalid",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else if (currentState !is UploadState.Uploading) {
                            if (viewModel.isDebugMode.value) {
                                viewModel.dummyGetScore(viewModel, UploadState.Success(77), 77)
                            } // simulate get score
                            else {
                                takeAndUploadPhoto(
                                    controller = controller,
                                    context = context,
                                    viewModel = viewModel
                                )
                            }
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
                onDismissRequest = { if (alertCode.value == AlertCode.GAME_OVER) viewModel.endGame() else openAlertDialog.value = false },
                onConfirmation = { openAlertDialog.value = false },
                dialogTitle = alertCode.toString(),
                dialogText = "This is an example of an alert dialog with buttons.",
                icon = Icons.Default.Info,
                alertCode = alertCode.value
            )
        }
    }
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
