package com.rgbcoding.yolodarts.data

import com.rgbcoding.yolodarts.presentation.AlertCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    private val _name: MutableStateFlow<String>,
    private val _scoreLeft: MutableStateFlow<Int> = MutableStateFlow(501),
    private val _throws: MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())
) {
    val name: StateFlow<String> = _name.asStateFlow()
    val scoreLeft: StateFlow<Int> = _scoreLeft.asStateFlow()
    val throws: StateFlow<List<Int>> = _throws.asStateFlow()

    constructor(playerName: String) : this(
        _name = MutableStateFlow(playerName)
    )

    fun hasWon(): Boolean = scoreLeft.value == 0
    fun nameChange(newName: String) {
        _name.value = newName
    }

    fun recordThrow(score: Int): AlertCode {
        val currentScore = _scoreLeft.value
        val newScore = currentScore - score
        if (newScore == 0) return AlertCode.GAME_OVER
        if (newScore < 2) return AlertCode.OVERSHOT
        _scoreLeft.value = newScore
        _throws.value += score
        return AlertCode.VALID_SCORE
    }

    fun undoLastThrow(): Int? {
        val lastThrow = _throws.value.lastOrNull() ?: return null // shouldnt ever happen lol
        _throws.value = _throws.value.dropLast(1)
        _scoreLeft.value += lastThrow
        return lastThrow
    }
}