package com.example.teumteum.data

data class TimeBlock(
    val startTime: Int,
    val endTime: Int,
    val type: TimeType // SLEEP, EVENT, EMPTY
)
