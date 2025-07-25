package com.example.teumteum.data

data class TimeBlock(
    val startMinutes: Int, // 0 ~ 1439 (10분 단위로 정규화 필요)
    val endMinutes: Int,
    val type: TimeType // SLEEP, EVENT, EMPTY
)
