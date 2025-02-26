package com.rgbcoding.yolodarts.domain

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YoloDartsTitleBar(
    onNavigationClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    TopAppBar(modifier = Modifier.height(80.dp), title = {
        Text(
            text = "YoloDarts", style = MaterialTheme.typography.titleMedium
        )
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground
    ), navigationIcon = {
        IconButton(
            onClick = {
                onNavigationClick()
            }) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "Navigation Drawer",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    },
        actions = {
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

@Preview
@Composable
fun previewTitleBar() {
    Box(
        modifier = Modifier.fillMaxSize()
    )
    {
        YoloDartsTitleBar({}, {})
    }
}