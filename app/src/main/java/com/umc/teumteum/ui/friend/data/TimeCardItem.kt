package com.umc.teumteum.ui.friend.data

data class TimeCardItem(
    val startTime: String,
    val endTime: String,
    val initialStartTime: String = startTime,
    val initialEndTime: String = endTime
)
