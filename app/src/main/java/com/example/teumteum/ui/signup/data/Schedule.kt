package com.example.teumteum.ui.signup.data

import java.time.LocalTime

data class Schedule(
    val title: String,
    val day: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String,
)