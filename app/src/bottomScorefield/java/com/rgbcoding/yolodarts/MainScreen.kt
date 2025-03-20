package com.rgbcoding.yolodarts

import android.widget.Toast
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DeveloperBoardOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NotStarted
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rgbcoding.yolodarts.domain.DrawerBody
import com.rgbcoding.yolodarts.domain.DrawerHeader
import com.rgbcoding.yolodarts.domain.MenuItem
import com.rgbcoding.yolodarts.domain.MenuItemType
import com.rgbcoding.yolodarts.domain.TitleBarUiState
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.domain.YoloDartsTitleBar
import com.rgbcoding.yolodarts.domain.toReadableString
import com.rgbcoding.yolodarts.presentation.BoardSquare
import com.rgbcoding.yolodarts.presentation.Crosshair
import com.rgbcoding.yolodarts.presentation.ExtensivePlayerCard
import com.rgbcoding.yolodarts.presentation.PlayerCard
import com.rgbcoding.yolodarts.presentation.ScoreTextField
import com.rgbcoding.yolodarts.services.rememberImeState
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
    val isAutoScoring by viewModel.autoScoringMode.collectAsState()
    val isAutoScoringMode by viewModel.autoScoringMode.collectAsState()
    val isDebugMode by viewModel.isDebugMode.collectAsState()


    //game logic
    val gameState by viewModel.gameState.collectAsState()
    val currentPlayerIndex by viewModel.currentPlayerIndex.collectAsState()

    // navigation drawer as a simple Settings Menu
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    //keyboard shift logic:
//    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
//    val cameraWeight = remember(keyboardVisible) {
//        if (keyboardVisible) 0.0000000001f else 1f
//    }
//    //other option:
//    val cameraWeightAnimated by animateFloatAsState(
//        targetValue = cameraWeight,
//        animationSpec = tween(durationMillis = 300)
//    )

    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                DrawerBody(
                    items = listOf(
                        MenuItem(
                            type = MenuItemType.BUTTON,
                            id = "Game Control",
                            value = "Game Control",
                            buttonAction = if (gameState == null) viewModel::startNewGame else viewModel::endGame,
                            text = if (gameState == null) "Start Game" else "End Game",
                            contentDescription = "Game Control",
                            icon = if (gameState == null) Icons.Default.NotStarted else Icons.Default.StopCircle
                        ),
                        MenuItem(
                            id = "ip",
                            value = serverIp,
                            text = "IP Address",
                            onValueChange = viewModel::setIp,
                            contentDescription = "Set IP Address",
                            icon = Icons.Default.Dns
                        ),
                        MenuItem(
                            id = "players",
                            value = playerCount,
                            text = "Number of Players",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
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
                            icon = if (isAutoScoringMode) Icons.Default.Cloud else Icons.Default.CloudOff
                        ),
                        MenuItem(
                            type = MenuItemType.BUTTON,
                            id = "debug mode",
                            value = "debug",
                            buttonAction = viewModel::toggleDebugMode,
                            text = if (isDebugMode) "Disable Debug Mode" else "Enable Debug Mode",
                            contentDescription = "Toggle Debug Mode",
                            icon = if (isDebugMode) Icons.Default.DeveloperBoard else Icons.Default.DeveloperBoardOff
                        ),
                    ),
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                YoloDartsTitleBar(
                    displayUploadState = {
                        Toast.makeText(context, "Current Upload State is ${uploadState.toReadableString()}", Toast.LENGTH_LONG).show()
                    },
                    onNavigationClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    onGalleryClick = {
                        showPhotos = !showPhotos
                    },
                    uiState = TitleBarUiState(uploadState, isAutoScoring)
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
                        .verticalScroll(scrollState),
                ) {
                    // Preview Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .animateContentSize() // Animate the size change
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
                            gameState?.let {
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
                            } ?: Button(
                                onClick = {
                                    viewModel.startNewGame()
                                },
                                Modifier.align(Alignment.Center)
                            )
                            {
                                Text("Start Game")
                            }
                        }

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .paint(
                                painterResource(id = R.drawable.yd_bg_square),
                                contentScale = ContentScale.Crop,
                            )
                    ) {
//                        var lockedHalfHeight by remember { mutableStateOf<Dp?>(null) }
//
//                        // Calculate only once
//                        LaunchedEffect(lockedHalfHeight) {
//                            if (lockedHalfHeight == null) {
//                                val totalHeight = maxHeight
//                                val remainingHeight = totalHeight - 64.dp
//                                lockedHalfHeight = remainingHeight / 2
//                            }
//                        }
//                        lockedHalfHeight?.let { halfHeight ->
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                //first player
                                val singlePlayerMode = gameState?.players?.size == 1
                                gameState?.let { game ->
                                    val currentPlayer = game.players[game.currentPlayerIndex.value]
                                    key(currentPlayer.id) {
                                        ExtensivePlayerCard(
                                            player = currentPlayer,
                                            isItsTurn = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
//                                                .height(if (singlePlayerMode) halfHeight * 2 else halfHeight),
                                            , showBull = singlePlayerMode // custom param
                                            //.height(if (gameState?.players?.size == 1) halfHeight * 2 else halfHeight) // oneplayer = full height
                                        )
                                    }
                                }
                                if (gameState?.players?.size == 2) {
                                    gameState?.let { game ->
                                        game.players.forEachIndexed { index, player ->
                                            if (index != currentPlayerIndex) {
                                                key(player.id) {
                                                    ExtensivePlayerCard(
                                                        player = player,
                                                        isItsTurn = false,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)
//                                                            .height(halfHeight)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Row( // all the other players
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
//                                            .height(halfHeight)
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        gameState?.let { game ->
                                            game.players.forEachIndexed { index, player ->
                                                if (index != currentPlayerIndex) {
                                                    key(player.id) {
                                                        PlayerCard(
                                                            player = player,
                                                            modifier = Modifier
                                                                .weight(1f / (playerCount.toInt() - 1))
                                                                .fillMaxHeight()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                                ScoreTextField(
                                    viewModel,
                                    isAutoScoringMode,
                                    controller,
                                    context,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .blur(if (gameState == null) 8.dp else 0.dp)
                                )
                        }
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
