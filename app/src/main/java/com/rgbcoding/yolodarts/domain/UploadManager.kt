package com.rgbcoding.yolodarts.domain

import android.util.Log
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
    data class Success(val score: Int) : UploadState()
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

    private val scoreListeners = mutableListOf<(Int) -> Unit>()

    fun setUploadState(newState: UploadState) {
        _uploadState.value = newState
    }

    fun addScoreListener(listener: (Int) -> Unit) {
        scoreListeners.add(listener)
    }

    fun removeScoreListener(listener: (Int) -> Unit) {
        scoreListeners.remove(listener)
    }

    fun enqueueUpload(request: Request) {
        uploadQueue.offer(request)
        processQueue()
    }

    private fun processQueue() {
        Log.d("uploadPhoto", "processing Queue of size: ${uploadQueue.size}")
        coroutineScope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (isProcessing) return@withLock
                isProcessing = true
            }

            while (uploadQueue.isNotEmpty()) {
                val request = uploadQueue.poll() ?: continue
                _uploadState.value = UploadState.Uploading()

                try {
                    Log.d("uploadPhoto", "Trying to call request with ip: ${request.url}")
                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()

                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val score = responseBody.toIntOrNull() ?: -1
                                _uploadState.value = UploadState.Success(score)
                                scoreListeners.forEach { it(score) }
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
                    Log.e("uploadPhoto", "Caught Error uploading photo: ${e.message}")
                    _uploadState.value = UploadState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
            // queue is empty
            mutex.withLock {
                isProcessing = false
                if (_uploadState.value !is UploadState.Error) _uploadState.value = UploadState.Idle
            }
        }
    }
}