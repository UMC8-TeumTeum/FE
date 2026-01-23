package com.umc.teumteum.ui.myhome.data

import java.time.LocalTime

data class MyRoutine (
    val routineId: Long,
    val title: String,
    val weekday: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String,
)