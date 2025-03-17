package com.rgbcoding.yolodarts.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Game(
    val players: List<Player>,
    var isGameOver: Boolean = false
) {
    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()
    val currentPlayer: Player get() = players[currentPlayerIndex.value]

    fun nextTurn() {
        if (players.size > 1) {
            _currentPlayerIndex.value = (currentPlayerIndex.value + 1) % players.size
        }
    }

    fun previousTurn() {
        if (players.size > 1) {
            _currentPlayerIndex.value = (currentPlayerIndex.value - 1 + players.size) % players.size
        }
    }

    companion object {
        const val STARTING_SCORE = 501 // TODO allow 301 or custom starting score
    }
}
