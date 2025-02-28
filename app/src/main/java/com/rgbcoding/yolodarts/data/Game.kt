package com.rgbcoding.yolodarts.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf

data class Game(
    val players: List<Player>,
    var currentPlayerIndex: MutableState<Int> = mutableIntStateOf(0),
    var isGameOver: Boolean = false
) {
    val currentPlayer: Player get() = players[currentPlayerIndex.value]

    fun nextTurn() {
        if (players.size > 1) {
            currentPlayerIndex.value = (currentPlayerIndex.value + 1) % players.size
        }
    }

    fun previousTurn() {
        if (players.size > 1) {
            currentPlayerIndex.value = (currentPlayerIndex.value - 1) % players.size
        }
    }

    companion object {
        const val STARTING_SCORE = 501 // TODO allow 301 or custom starting score
    }
}
