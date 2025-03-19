package com.rgbcoding.yolodarts

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rgbcoding.yolodarts.data.Game
import com.rgbcoding.yolodarts.data.Player
import com.rgbcoding.yolodarts.data.PreferencesManager
import com.rgbcoding.yolodarts.domain.UploadManager
import com.rgbcoding.yolodarts.domain.UploadState
import com.rgbcoding.yolodarts.domain.toReadableString
import com.rgbcoding.yolodarts.presentation.AlertCode
import com.rgbcoding.yolodarts.services.SpeechService
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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)

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

    private val _serverIp = MutableStateFlow(preferencesManager.getServerIp())
    val serverIp: StateFlow<String> = _serverIp.asStateFlow()

    private val _playerCount = MutableStateFlow(preferencesManager.getPlayerCount())
    val playerCount: StateFlow<String> = _playerCount.asStateFlow()

    private val _lastScore = MutableStateFlow<Int?>(null)
    val lastScore = _lastScore.asStateFlow()

    private val _scoreValidationError = MutableStateFlow<String?>(null)
    val scoreValidationError = _scoreValidationError.asStateFlow()

    private val _currentPhoto = MutableStateFlow<Bitmap?>(null)
    val currentPhoto: StateFlow<Bitmap?> = _currentPhoto

    private var pendingScoreOverride = ""

    private var pendingScore: String? = null

    private val _autoScoringMode = MutableStateFlow(preferencesManager.getAutoScoringMode())
    val autoScoringMode = _autoScoringMode.asStateFlow()

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()

    private val _isDebugMode = MutableStateFlow(preferencesManager.getDebugMode())
    val isDebugMode = _isDebugMode.asStateFlow()

    // lifecycle functions

    init {
        // Register score listener
        uploadManager.addScoreListener { newScore ->
            viewModelScope.launch {
                _lastScore.update {
                    newScore
                }
                SpeechService.speakText(newScore.toString())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up listener when ViewModel is cleared
        uploadManager.removeScoreListener { newScore ->
            viewModelScope.launch {
                _lastScore.update {
                    newScore
                }
            }
        }
    }

    // functions

    fun setIp(ip: String) {
        _serverIp.value = ip
        preferencesManager.saveServerIp(ip)
    }

    fun updatePlayers(playerCount: String) {
        if (_gameState.value != null) return
        _playerCount.value = playerCount
        preferencesManager.savePlayerCount(playerCount)
    }

    fun toggleAutoScoring() {
        _autoScoringMode.value = !(_autoScoringMode.value)
        preferencesManager.saveAutoScoringMode(_autoScoringMode.value)
    }

    fun toggleDebugMode() {
        _isDebugMode.value = !(_isDebugMode.value)
        preferencesManager.saveDebugMode(_isDebugMode.value)
    }

    fun setUploadState(newState: UploadState) {
        Log.d("Scoring", "ViewModel is trying to setuploadstate to $newState")
        uploadManager.setUploadState(newState)
    }

    private fun validScore(score: Int?): Boolean {
        when {
            score == null -> {
                return false
            }

            score < 0 || score > 180 -> {
                return false
            }
        }
        return true
    }

    fun validateScoreInTextfield(scoreText: String) {
        val scoreValue = scoreText.toIntOrNull()
        when {
            scoreText.isEmpty() -> {
                _scoreValidationError.value = null
                pendingScore = null
            }

            !validScore(scoreValue) -> {
                _scoreValidationError.value = "Score must be an Integer between 0-180"
                pendingScore = null
            }

            else -> {
                _scoreValidationError.value = null
                pendingScore = scoreText
            }
        }
    }

    fun submitScore(scoreText: String? = null): AlertCode {
        val finalScore = scoreText ?: pendingScore

        // Basic validation
        val scoreValue = finalScore?.toIntOrNull() ?: return AlertCode.INVALID_SCORE

        //validate score again
        if (!validScore(scoreValue)) return AlertCode.INVALID_SCORE

        // Game validation
        val game = gameState.value ?: return AlertCode.NO_GAME
        val currentPlayer = game.currentPlayer

        // Game rules validation
        if (scoreValue > currentPlayer.scoreLeft.value) {
            return AlertCode.OVERSHOT
        }

        // Record the throw
        val recordedThrowAlert = currentPlayer.recordThrow(scoreValue)
        if (recordedThrowAlert != AlertCode.VALID_SCORE) {
            return recordedThrowAlert
        }

        // Speak the score
        SpeechService.speakText(finalScore.toString())

        // Reset states
        pendingScore = null
        _lastScore.value = null
        uploadManager.setUploadState(UploadState.Idle)

        // Check for game end
        if (currentPlayer.hasWon()) {
            return AlertCode.GAME_OVER
        }

        // Move to next turn
        game.nextTurn()
        _currentPlayerIndex.value = game.currentPlayerIndex.value

        return AlertCode.VALID_SCORE
    }

    fun updateLastScore(score: Int) {
        _lastScore.value = score
        pendingScore = score.toString()
        _scoreValidationError.value = null
    }

    //game logic:

    fun startNewGame() {
        val playerNames = playerCount.value.toIntOrNull()?.let { count ->
            generatePlayerNames(count)
        } ?: run {
            updatePlayers("1")
            generatePlayerNames(1)
        }
        val players = playerNames.map { name -> Player(name) }
        _gameState.value = Game(players)
    }

    private fun generatePlayerNames(playerCount: Int): List<String> {
        return List(playerCount) { index -> "Player ${index + 1}" }
    }

    fun goBack() {
        val game = _gameState.value ?: return

        // One player case - directly undo throws
        if (game.players.size == 1) {
            val undoneThrow = game.currentPlayer.undoLastThrow()
            Log.d("undo", "Single player mode - Undone throw: $undoneThrow")
            _lastScore.value = undoneThrow // put the undone throw into the textfield
            _currentPlayerIndex.value = game.currentPlayerIndex.value // force recomposition
            return
        }

        if (game.currentPlayer.throws.value.isEmpty() && game.currentPlayerIndex.value == 0) {
            // Reset states
            pendingScore = null
            _lastScore.value = null
            uploadManager.setUploadState(UploadState.Idle)
            Log.d("Undo", "all the way at the start already")
            return //if no throws logged and its the first player stop going back
        }
        game.previousTurn()
        val undoneThrow = game.currentPlayer.undoLastThrow()
        _lastScore.value = undoneThrow // put the undone throw into the textfield
        Log.d("undo", "undo press - undone throw: $undoneThrow")

        // Force recomposition
        _currentPlayerIndex.value = game.currentPlayerIndex.value
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

        val ipRegex = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

        val requestUrl = if (ipRegex.matches(serverIp.value.trim())) {
            "http://${serverIp.value.trim()}:5000/"
        } else {
            "https://${serverIp.value.trim()}/"
        }
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        uploadManager.enqueueUpload(request)
    }

    fun dummyGetScore(
        viewModel: MainViewModel,
        uploadState: UploadState,
        returnScore: Int?
    ) {
        Log.d("Scoring", "dummyGetScore is setting uploadstate from ${viewModel.uploadState.value.toReadableString()} to ${uploadState.toReadableString()}")
        viewModel.setUploadState(uploadState)
        viewModel._lastScore.value = returnScore
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