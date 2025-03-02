package com.rgbcoding.yolodarts

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.rgbcoding.yolodarts.presentation.PlayerCard
import com.rgbcoding.yolodarts.presentation.ScoreTextField
import com.rgbcoding.yolodarts.presentation.GetScoreButton
import com.rgbcoding.yolodarts.ui.theme.Shapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }
    }
    
    val gameState by viewModel.gameState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val isProcessing = uploadState is UploadState.Uploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "YoloDarts",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Camera Preview Section (Top Half)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(Shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    CameraPreview(
                        controller = controller,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Crosshair overlay
                    Crosshair(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )

                    // Score overlay when processing
                    AnimatedVisibility(
                        visible = uploadState is UploadState.Success,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            shape = Shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "Score: ${(uploadState as? UploadState.Success)?.score ?: ""}",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Game State Section (Bottom Half)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Active Player
                            gameState?.currentPlayer?.let { player ->
                                PlayerCard(
                                    name = player.name.value,
                                    scoreLeft = player.scoreLeft.value,
                                    average = if (player.throws.value.isEmpty()) 0.0 
                                            else player.throws.value.average(),
                                    dartsThrown = player.throws.value.size,
                                    isActive = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Other Players
                            gameState?.players?.filter { it != gameState?.currentPlayer }?.forEach { player ->
                                PlayerCard(
                                    name = player.name.value,
                                    scoreLeft = player.scoreLeft.value,
                                    average = if (player.throws.value.isEmpty()) 0.0 
                                            else player.throws.value.average(),
                                    dartsThrown = player.throws.value.size,
                                    isActive = false
                                )
                            }
                        }

                        // Get Score Button
                        GetScoreButton(
                            onClick = {
                                // Implement score capture logic
                            },
                            isProcessing = isProcessing,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}



