package com.rgbcoding.yolodarts

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.WifiPassword
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rgbcoding.yolodarts.domain.DrawerBody
import com.rgbcoding.yolodarts.domain.DrawerHeader
import com.rgbcoding.yolodarts.domain.MenuItem
import com.rgbcoding.yolodarts.domain.MenuItemType
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.domain.YoloDartsTitleBar
import com.rgbcoding.yolodarts.presentation.BoardSquare
import com.rgbcoding.yolodarts.presentation.Crosshair
import com.rgbcoding.yolodarts.presentation.ScoreTextField
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
    var showPhotos by remember { mutableStateOf(false) }
    val serverIp by viewModel.serverIp.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val isAutoScoringMode by viewModel.autoScoringMode.collectAsState()

    // navigation drawer as a simple Settings Menu
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                DrawerBody(
                    items = listOf(
                        MenuItem(
                            id = "ip",
                            value = serverIp,
                            text = "IP Address",
                            onValueChange = viewModel::setIp,
                            contentDescription = "Set IP Address",
                            icon = Icons.Default.WifiPassword
                        ),
                        MenuItem(
                            id = "players",
                            value = playerCount,
                            text = "Number of Players",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            onValueChange = viewModel::updatePlayers,
                            contentDescription = "Set Player Count",
                            icon = Icons.Default.PersonAdd
                        ),
                        MenuItem(
                            type = MenuItemType.BUTTON,
                            id = "scoring mode",
                            value = "ai",
                            buttonAction = viewModel::toggleAutoScoring,
                            text = if (isAutoScoringMode) "Disable AI Scoring" else "Enable AI Scoring",
                            contentDescription = "Toggle AI Scoring Mode",
                            icon = if (isAutoScoringMode) Icons.Default.AirplanemodeActive else Icons.Default.AirplanemodeInactive
                        ),
                    ),
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                YoloDartsTitleBar(
                    onNavigationClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    onGalleryClick = {
                        showPhotos = !showPhotos
                    }
                )
            }
        ) { padding ->
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
                                .padding(
                                    top = 0.dp,
                                    bottom = 0.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                )
                        )
                        BoardSquare(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                ) {
                                    Text(
                                        "Error: ${(uploadState as UploadState.Error).message}",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Crosshair(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                )
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

                        ScoreTextField(
                            viewModel,
                            isAutoScoringMode,
                            uploadState,
                            controller,
                            context,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
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
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp
                                        )
                                    )
                            )
                        }
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


