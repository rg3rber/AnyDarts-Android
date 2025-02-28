package com.rgbcoding.yolodarts.data

data class GameScore(
    var scoreLeft: Int,
    var scoreHistory: List<Int>?,
    var lastscore: Int,
    var threeDartAvg: Float,
)
