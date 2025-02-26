package com.rgbcoding.yolodarts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rgbcoding.yolodarts.domain.UploadManager
import com.rgbcoding.yolodarts.domain.UploadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    // attributes

    private val client = OkHttpClient.Builder()
        .hostnameVerifier { _, _ -> true }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val uploadManager = UploadManager(client, viewModelScope)
    val uploadState = uploadManager.uploadState

    private val _lastPhotos = MutableStateFlow<List<Bitmap>>(emptyList())
    val lastPhotos = _lastPhotos.asStateFlow()

    private val _serverIp = MutableStateFlow("192.168.178.111")
    val serverIp: StateFlow<String> = _serverIp.asStateFlow()

    private val _playerCount = MutableStateFlow("1")
    val playerCount: StateFlow<String> = _playerCount.asStateFlow()

    private val _lastScores = MutableStateFlow<List<Int>>(emptyList())
    val lastScores = _lastScores.asStateFlow()

    private val _currentPhoto = MutableStateFlow<Bitmap?>(null)
    val currentPhoto: StateFlow<Bitmap?> = _currentPhoto

    private var pendingScoreOverride = ""

    private var _autoScoringMode = MutableStateFlow<Boolean>(true)
    val autoScoringMode = _autoScoringMode.asStateFlow()

    // lifecycle functions

    init {
        // Register score listener
        uploadManager.addScoreListener { newScore ->
            viewModelScope.launch {
                _lastScores.update { currentScores ->
                    (currentScores + newScore)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up listener when ViewModel is cleared
        uploadManager.removeScoreListener { newScore ->
            viewModelScope.launch {
                _lastScores.update { currentScores ->
                    (currentScores + newScore).takeLast(10)
                }
            }
        }
    }

    // functions

    fun setIp(ip: String) {
        _serverIp.value = ip
    }

    fun updatePlayers(playerCount: String) {
        _playerCount.value = playerCount
    }

    fun toggleAutoScoring() {
        _autoScoringMode.value = !(_autoScoringMode.value)
    }

    fun overrideScore(newScore: String): Boolean {
        val isError = newScore.toIntOrNull() == null || newScore.toInt() < 0
        if (!isError) pendingScoreOverride = newScore
        return isError
    }

    fun submitScoreOverride() {
        val scoreValue = pendingScoreOverride.toIntOrNull() // better safe than sorry
        if (scoreValue != null) {
            viewModelScope.launch {
                _lastScores.update { scores ->
                    if (scores.isEmpty()) listOf(scoreValue)
                    else {
                        scores.toMutableList().apply {
                            this[lastIndex] = scoreValue
                        }
                    }
                }
            }
        }
        pendingScoreOverride = ""
        uploadManager.setUploadState(UploadState.Idle) // after submitting score reset uploadstate
        // TODO: change players turn here
    }

    // Photo Management

    fun onTakePhoto(bitmap: Bitmap) {
        _lastPhotos.update { currentPhotos ->
            val maxPhotos = 1
            (currentPhotos + bitmap).takeLast(maxPhotos)
        }
    }

    fun uploadPhoto(
        photo: Bitmap
    ) {
        Log.e("uploadPhoto", "uploading image...")
        // Convert Bitmap to byte array
        val outputStream = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val photoBytes = outputStream.toByteArray()

        // Create multipart request body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "dartboard_image.jpg",
                photoBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        // Placeholder URL - replace with your actual server address
        val request = Request.Builder()
            .url("http://${serverIp.value}:5000/")
            .post(requestBody)
            .build()

        uploadManager.enqueueUpload(request)
    }
}

fun takeAndUploadPhoto(
    controller: LifecycleCameraController,
    context: Context,
    viewModel: MainViewModel
) {
    takePhoto(
        controller = controller,
        onPhotoTaken = { bitmap -> // This callback is invoked after the photo is taken
            viewModel.onTakePhoto(bitmap)
            viewModel.uploadPhoto(
                photo = bitmap
            )
        },
        context = context
    )
}

fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat()) // rotate
                    //postScale(1f, 1f) somehow not needed when uploading to server
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onPhotoTaken(rotatedBitmap)
                image.close() //close image
                //Toast.makeText(context, "Took a photo!", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "🚨Couldn't take photo ", exception)
            }
        }
    )
}