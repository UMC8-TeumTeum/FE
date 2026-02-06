package com.umc.teumteum.ui.onboarding.data

import java.time.LocalTime
import java.util.UUID

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val day: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String,
)