package com.rgbcoding.yolodarts.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale


object SpeechService : TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
    private var isInitialized = false

    fun initialize(context: Context) {
        textToSpeech = TextToSpeech(context, this)
    }

    fun speakText(text: String) {
        if (isInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TextToSpeech", "TTS not initialized yet")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true

            // Set language to English (US)
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language not supported")
            }

            // Select a male voice if available
            val targetVoice = "en-us-x-tpf-local"
            val voices = textToSpeech.voices?.filter { it.locale.language == "en" } ?: emptyList()

            val preferredVoice = voices.firstOrNull { it.name.contains("tpf", ignoreCase = true) }

            val selectedVoice = preferredVoice ?: voices.firstOrNull()

            if (selectedVoice != null) {
                textToSpeech.voice = selectedVoice
            } else {
                Log.w("TextToSpeech", "No male voice found, using default voice")
            }

        } else {
            Log.e("TextToSpeech", "TTS initialization failed")
        }
    }

    fun shutdown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}