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
import com.rgbcoding.yolodarts.data.Game
import com.rgbcoding.yolodarts.data.Player
import com.rgbcoding.yolodarts.domain.UploadManager
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.presentation.AlertCode
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

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

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

    fun setUploadState(newState: UploadState) {
        uploadManager.setUploadState(newState)
    }

    fun overrideScore(newScore: String): Boolean {
        val isError = newScore.toIntOrNull() == null || newScore.toInt() < 0 || newScore.toInt() > 180
        if (!isError) pendingScoreOverride = newScore
        return isError
    }

    fun submitScoreOverride(): AlertCode {
        val scoreValue = pendingScoreOverride.toIntOrNull() ?: return AlertCode.INVALID_SCORE// better safe than sorry

        val currentPlayer = gameState.value!!.currentPlayer

        if (scoreValue > currentPlayer.scoreLeft.value) {
            return AlertCode.OVERSHOT
        }
        currentPlayer.throws.value += scoreValue
        currentPlayer.scoreLeft.value -= scoreValue

        pendingScoreOverride = ""
        Log.d("Scoring", "setting uploadstate to idle")
        uploadManager.setUploadState(UploadState.Idle) // after submitting score reset uploadstate

        if (currentPlayer.hasWon()) {
            return AlertCode.GAME_OVER
        } else {
            _gameState.value!!.nextTurn()
            return AlertCode.VALID_SCORE
        }
    }

    //game logic:

    fun startNewGame() {
        val playerNames = generatePlayerNames(playerCount.value.toInt())
        val players = playerNames.map { name -> Player(name) }
        _gameState.value = Game(players)
    }

    private fun generatePlayerNames(playerCount: Int): List<String> {
        return List(playerCount) { index -> "Player ${index + 1}" }
    }

    fun goBack() {

        if (_gameState.value == null) return // return if no game
        else {
            val currentPlayer = _gameState.value!!.currentPlayer
            val currentPlayerIndex = _gameState.value!!.currentPlayerIndex
            if (currentPlayer.throws.value.isEmpty() && currentPlayerIndex.value == 0) return // only undo if there is something to undo

            _gameState.value!!.previousTurn() // set player turn back

            val undoneThrow = currentPlayer.throws.value.lastOrNull()
            undoneThrow?.let {
                currentPlayer.throws.value = currentPlayer.throws.value.dropLast(1) // drop last throw
                currentPlayer.scoreLeft.value += it // reset score left
                setUploadState(UploadState.Success(it)) // put the "dropped" throw into score textfield value
            } ?: return
        }

    }

    fun endGame() {
        // TODO save the game in Room
        _gameState.value = null
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