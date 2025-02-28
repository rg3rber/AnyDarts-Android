package com.rgbcoding.yolodarts.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

data class Player(
    var name: MutableState<String>,
    var scoreLeft: MutableState<Int> = mutableIntStateOf(501),
    val throws: MutableState<List<Int>> = mutableStateOf(emptyList())
) {
    constructor(playerName: String) : this(
        name = mutableStateOf(playerName),
        scoreLeft = mutableIntStateOf(501),
        throws = mutableStateOf(emptyList())
    )

    fun hasWon(): Boolean = scoreLeft.value == 0
    fun nameChange(newName: String) {
        name.value = newName
    }
}