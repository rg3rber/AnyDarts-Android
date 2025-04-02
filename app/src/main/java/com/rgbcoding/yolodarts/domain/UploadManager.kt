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
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

// Sealed class to represent different upload states
sealed class UploadState {
    object Idle : UploadState()
    data class Uploading(val progress: Int = 0) : UploadState()
    data class Success(val score: Int) : UploadState()
    data class Error(val message: String) : UploadState()
}

fun UploadState.toReadableString(): String {
    return when (this) {
        is UploadState.Success -> "Success (Score: $score)"
        is UploadState.Uploading -> "Uploading (${progress}%)"
        is UploadState.Error -> "Error: $message"
        is UploadState.Idle -> "Idle"
    }
}

class UploadManager(
    private val client: OkHttpClient,
    private val coroutineScope: CoroutineScope
) {
    private val uploadQueue = ConcurrentLinkedQueue<Request>()
    private val mutex = Mutex()
    private var isProcessing = false

    //TODO add retry logic ie. after getting Uploadstate.Error user can try again or manually set score

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val scoreListeners = mutableListOf<(Int) -> Unit>()

    fun setUploadState(newState: UploadState) {
        Log.d("Scoring", "UploadManager is setting uploadState from ${uploadState.value.toReadableString()} to $newState")
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
                    val timeoutClient = client.newBuilder()
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build()

                    timeoutClient.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string()

                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val score = responseBody.toIntOrNull()
                                if (score == null) {
                                    _uploadState.value = UploadState.Error("Failed to detect Board")
                                } else {
                                    _uploadState.value = UploadState.Success(score)
                                    scoreListeners.forEach { it(score) }
                                }
                            } catch (e: Exception) {
                                _uploadState.value = UploadState.Error("Error processing score")
                            }
                        } else {
                            when (response.code) {
                                500 -> _uploadState.value = UploadState.Error(
                                    "Could not process uploaded image. Make sure the board is clearly visible"
                                )

                                else -> _uploadState.value = UploadState.Error(
                                    "Upload failed. Check your Connection and try again"
                                )
                            }
                            Log.e("Upload", "Upload failed. Response code: ${response.code} and message: ${response.message}")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("uploadPhoto", "Caught Error uploading photo: ${e.message}")
                    _uploadState.value = UploadState.Error(e.localizedMessage ?: "Unknown error")
                } catch (e: SocketTimeoutException) {
                    Log.e("uploadPhoto", "Request timed out: ${e.message}")
                    _uploadState.value = UploadState.Error("Request timed out. Please try again.")
                } catch (e: Exception) {
                    Log.e("uploadPhoto", "Unexpected error: ${e.message}")
                    _uploadState.value = UploadState.Error("An unexpected error occurred")
                }
            }
            // queue is empty
            mutex.withLock {
                isProcessing = false
                Log.d("UploadManager:", "emptied Queue and uploadState is ${_uploadState.value}")

                // TODO idk if this is correct? Safety net: If we somehow still have Uploading state when queue is empty, reset to Idle
                if (_uploadState.value is UploadState.Uploading) {
                    _uploadState.value = UploadState.Idle
                }
            }
        }
    }
}