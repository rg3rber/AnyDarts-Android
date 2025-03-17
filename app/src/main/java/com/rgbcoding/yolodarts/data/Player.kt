package com.rgbcoding.yolodarts.data

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

    fun customCopy(
        id: String = this.id,
        _name: MutableStateFlow<String> = this._name,
        _scoreLeft: MutableStateFlow<Int> = this._scoreLeft,
        _throws: MutableStateFlow<List<Int>> = this._throws
    ): Player {
        return Player(id, _name, _scoreLeft, _throws)
    }

    constructor(playerName: String) : this(
        _name = MutableStateFlow(playerName)
    )

    fun hasWon(): Boolean = scoreLeft.value == 0
    fun nameChange(newName: String) {
        _name.value = newName
    }

    fun recordThrow(score: Int) {
        val currentScore = _scoreLeft.value
        val newScore = maxOf(0, currentScore - score)
        _scoreLeft.value = newScore
        _throws.value += score
    }

    fun undoLastThrow(): Int? {
        val lastThrow = _throws.value.lastOrNull() ?: return null
        _throws.value = _throws.value.dropLast(1)
        _scoreLeft.value += lastThrow
        return lastThrow
    }
}