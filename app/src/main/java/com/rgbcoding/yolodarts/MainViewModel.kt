package com.rgbcoding.yolodarts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    private val _serverIp = MutableStateFlow("192.168.178.111")
    val serverIp: StateFlow<String> = _serverIp.asStateFlow()

    fun setIp(ip: String) {
        _serverIp.value = ip
    }

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }
}

fun uploadPhoto(
    lastPhoto: Bitmap,
    viewModel: MainViewModel,
    showToast: (String) -> Unit
) {
    // Convert Bitmap to byte array
    val outputStream = ByteArrayOutputStream()
    lastPhoto.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
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

    // OkHttp client for network request
    val client = OkHttpClient.Builder()
        .hostnameVerifier { _, _ -> true }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Placeholder URL - replace with your actual server address
    val request = Request.Builder()
        .url("http://${viewModel.serverIp.value}:5000/")
        .post(requestBody)
        .build()

    // Asynchronous network call
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            showToast(
                "Upload failed: ${e.localizedMessage}"
            )
            Log.e("PhotoUpload", "Error uploading photo", e)
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                try {
                    val score = responseBody.toIntOrNull() ?: -1
                    showToast(
                        "Upload successful! Score: $score"
                    )
                } catch (e: Exception) {
                    showToast(
                        "Error processing score",
                    )
                }
            } else {
                showToast("Connection works but upload failed: Code ${response.code}")
                Log.e("PhotoUpload", "Error: ${response.code} - $responseBody")
            }
        }

    })
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

                Toast.makeText(context, "Took a photo!", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo ", exception)
            }
        }
    )
}