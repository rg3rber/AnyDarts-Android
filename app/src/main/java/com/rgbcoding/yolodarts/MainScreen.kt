package com.rgbcoding.yolodarts

import android.widget.Toast
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.presentation.Crosshair
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }
    }
    val lastPhotos by viewModel.lastPhotos.collectAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState() // TODO remove?
    var showPhotos by remember { mutableStateOf(false) }
    val serverIp by viewModel.serverIp.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    Scaffold(topBar = {
        TopAppBar(modifier = Modifier.height(80.dp), title = {
            Text(
                text = "Camera Preview", style = MaterialTheme.typography.titleMedium
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ), actions = {
            // Optional: Add action icons here
            IconButton(onClick = {
                showPhotos = !showPhotos
                // Handle gallery open
                scope.launch {

                }
            }) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Open Gallery",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        })
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CameraPreview(
                        controller = controller,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                            .padding(top = 0.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                    )
                    when (uploadState) {
                        is UploadState.Uploading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Text(
                                    "Uploading...",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(top = 48.dp)
                                )
                            }
                        }

                        is UploadState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                            ) {
                                Text(
                                    "Score: ${(uploadState as UploadState.Success).score}",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        is UploadState.Error -> {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Error: ${(uploadState as UploadState.Error).message}",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        is UploadState.Idle -> {
                            Crosshair(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    val screenHeight = maxHeight
                    val verticalOffset = screenHeight * 0.25f
                    Button(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(0.dp, -verticalOffset),
                        onClick = {
                            if (uploadState is UploadState.Idle) {
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
                        },
                        colors = buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            "Get Score",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    TextField(
                        value = if (lastScore.isEmpty()) "-1" else lastScore.last().toString(),
                        onValueChange = viewModel::setScore,
                        label = {
                            Text(
                                "Last score:",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.Center)
                            .padding(16.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    TextField(
                        value = serverIp,
                        onValueChange = viewModel::setIp,
                        label = {
                            Text(
                                "Enter server IP:",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .offset(0.dp, (-64).dp)
                            .padding(16.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    this@Column.AnimatedVisibility(
                        visible = showPhotos,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        PhotoBottomSheetContent(
                            lastPhotos = lastPhotos,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun previewMain() {
//    YoloDartsTheme {
//        MainScreen()
//    }
//}


