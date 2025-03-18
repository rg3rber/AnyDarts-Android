package com.rgbcoding.yolodarts.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(private val context: Context) {
    companion object {

        private const val PREFS_NAME = "app_preferences"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_PLAYER_COUNT = "player_count"
        private const val KEY_AUTO_SCORING_MODE = "auto_scoring_mode"
        private const val DEFAULT_IP = "192.168.178.111"
        private const val DEFAULT_PLAYER_COUNT = "1"
        private const val DEFAULT_AUTO_SCORING_MODE = true
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Server IP
    fun getServerIp(): String = sharedPreferences.getString(KEY_SERVER_IP, DEFAULT_IP) ?: DEFAULT_IP

    fun saveServerIp(ip: String) {
        sharedPreferences.edit().putString(KEY_SERVER_IP, ip).apply()
    }

    // Player Count
    fun getPlayerCount(): String = sharedPreferences.getString(KEY_PLAYER_COUNT, DEFAULT_PLAYER_COUNT) ?: DEFAULT_PLAYER_COUNT

    fun savePlayerCount(count: String) {
        sharedPreferences.edit().putString(KEY_PLAYER_COUNT, count).apply()
    }

    // Auto Scoring Mode
    fun getAutoScoringMode(): Boolean = sharedPreferences.getBoolean(KEY_AUTO_SCORING_MODE, DEFAULT_AUTO_SCORING_MODE)

    fun saveAutoScoringMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_SCORING_MODE, enabled).apply()
    }
}