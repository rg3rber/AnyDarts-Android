package com.rgbcoding.yolodarts

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class PhotoUploadTest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun testSuccessfulPhotoUpload() {
        // Enqueue a mock response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("10")  // Simulated dart score
        )

        // Get the mock server's URL
        val baseUrl = mockWebServer.url("/").toString()

        // Perform upload with mock server URL
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "test_image.jpg",
                // Create a sample request body
                ByteArray(1024).toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
            .build()

        // Execute the request
        val response = client.newCall(request).execute()

        // Assert response
        assert(response.isSuccessful)
        assert(response.body?.string() == "10")
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}