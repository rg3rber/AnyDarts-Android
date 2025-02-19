package com.rgbcoding.yolodarts.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue

// Sealed class to represent different upload states
sealed class UploadState {
    object Idle : UploadState()
    data class Uploading(val progress: Int = 0) : UploadState()
    data class Success(val score: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

class UploadManager(
    private val client: OkHttpClient,
    private val coroutineScope: CoroutineScope
) {
    private val uploadQueue = ConcurrentLinkedQueue<Request>()
    private val mutex = Mutex()
    private var isProcessing = false

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun enqueueUpload(request: Request) {
        uploadQueue.offer(request)
        processQueue()
    }

    private fun processQueue() {
        coroutineScope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (isProcessing) return@withLock
                isProcessing = true
            }

            while (uploadQueue.isNotEmpty()) {
                val request = uploadQueue.poll() ?: continue
                _uploadState.value = UploadState.Uploading()

                try {
                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()

                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val score = responseBody.toIntOrNull() ?: -1
                                _uploadState.value = UploadState.Success(score.toString())
                            } catch (e: Exception) {
                                _uploadState.value = UploadState.Error("Error processing score")
                            }
                        } else {
                            _uploadState.value = UploadState.Error(
                                "Upload failed: ${response.code}"
                            )
                        }
                    }
                } catch (e: IOException) {
                    _uploadState.value = UploadState.Error(e.localizedMessage ?: "Unknown error")
                }
            }

            mutex.withLock {
                isProcessing = false
                _uploadState.value = UploadState.Idle
            }
        }
    }
}