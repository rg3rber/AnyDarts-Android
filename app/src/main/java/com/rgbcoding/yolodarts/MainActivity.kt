package com.rgbcoding.yolodarts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rgbcoding.yolodarts.services.SpeechService
import com.rgbcoding.yolodarts.ui.theme.YoloDartsTheme

class MainActivity : ComponentActivity() {

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                0
            ) //TODO: add actual permission handling
        }
        enableEdgeToEdge()

        SpeechService.initialize(applicationContext)

        setContent {
            YoloDartsTheme {
                MainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechService.shutdown()
    }

    private fun hasRequiredPermissions(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}