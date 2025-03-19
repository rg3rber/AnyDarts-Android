package com.rgbcoding.yolodarts.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(private val context: Context) {
    companion object {

        private const val PREFS_NAME = "app_preferences"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_PLAYER_COUNT = "player_count"
        private const val KEY_AUTO_SCORING_MODE = "auto_scoring_mode"
        private const val KEY_DEBUG_MODE = "debug_mode"
        private const val DEFAULT_IP = "yolodarts.pythonanywhere.com"
        private const val DEFAULT_PLAYER_COUNT = "1"
        private const val DEFAULT_AUTO_SCORING_MODE = true
        private const val DEFAULT_DEBUG_MODE = false
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Server IP
    fun getServerIp(): String = sharedPreferences.getString(KEY_SERVER_IP, DEFAULT_IP) ?: DEFAULT_IP

    fun saveServerIp(ip: String) {
        sharedPreferences.edit() { putString(KEY_SERVER_IP, ip) }
    }

    // Player Count
    fun getPlayerCount(): String = sharedPreferences.getString(KEY_PLAYER_COUNT, DEFAULT_PLAYER_COUNT) ?: DEFAULT_PLAYER_COUNT

    fun savePlayerCount(count: String) {
        sharedPreferences.edit() { putString(KEY_PLAYER_COUNT, count) }
    }

    // Auto Scoring Mode
    fun getAutoScoringMode(): Boolean = sharedPreferences.getBoolean(KEY_AUTO_SCORING_MODE, DEFAULT_AUTO_SCORING_MODE)

    fun saveAutoScoringMode(enabled: Boolean) {
        sharedPreferences.edit() { putBoolean(KEY_AUTO_SCORING_MODE, enabled) }
    }

    // Debug (to turn on or off simulation of server)

    fun getDebugMode(): Boolean = sharedPreferences.getBoolean(KEY_DEBUG_MODE, DEFAULT_DEBUG_MODE)

    fun saveDebugMode(enabled: Boolean) {
        sharedPreferences.edit() { putBoolean(KEY_DEBUG_MODE, enabled) }
    }

}