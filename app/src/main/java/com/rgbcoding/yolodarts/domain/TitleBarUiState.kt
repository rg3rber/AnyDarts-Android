package com.rgbcoding.yolodarts.domain

data class TitleBarUiState(
    val uploadState: UploadState = UploadState.Idle,
    val isAutoScoring: Boolean = false
)
