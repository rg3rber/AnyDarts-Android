package com.rgbcoding.yolodarts.domain

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rgbcoding.yolodarts.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YoloDartsTitleBar(
    displayUploadState: () -> Unit,
    onNavigationClick: () -> Unit,
    onGalleryClick: () -> Unit,
    uiState: TitleBarUiState,
) {

    TopAppBar(
        modifier = Modifier, title = {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.anydarts_logo),
                    contentDescription = "Title Image",
                    modifier = Modifier.size(140.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            IconButton(
                onClick = {
                    onNavigationClick()
                }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Navigation Drawer",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    displayUploadState()
                }
            ) {
                Icon(
                    imageVector = when (uiState.uploadState) {
                        is UploadState.Success -> Icons.Default.CloudDone
                        is UploadState.Uploading -> Icons.Default.CloudUpload
                        is UploadState.Error -> Icons.Default.Cloud
                        is UploadState.Idle -> if (uiState.isAutoScoring) Icons.Default.CloudQueue else Icons.Default.CloudOff
                    },
                    contentDescription = "Upload State Icon",
                    tint = when (uiState.uploadState) {
                        is UploadState.Success -> Color.Green
                        is UploadState.Uploading -> Color.Yellow
                        is UploadState.Error -> MaterialTheme.colorScheme.error
                        is UploadState.Idle -> MaterialTheme.colorScheme.onBackground
                    }

                )
            }
            // Optional: Add action icons here
            IconButton(onClick = {
                onGalleryClick()
            }) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Open Gallery",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTitleBar() {
    val mockUiState = TitleBarUiState(
        uploadState = UploadState.Success(10),
        isAutoScoring = true
    )

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            YoloDartsTitleBar(
                displayUploadState = {},
                onNavigationClick = {},
                onGalleryClick = {},
                uiState = mockUiState
            )
        }
    }
}
