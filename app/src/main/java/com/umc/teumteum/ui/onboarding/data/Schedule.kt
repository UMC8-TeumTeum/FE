package com.umc.teumteum.ui.onboarding.data

import java.time.LocalTime

data class Schedule(
    val title: String,
    val day: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String,
)