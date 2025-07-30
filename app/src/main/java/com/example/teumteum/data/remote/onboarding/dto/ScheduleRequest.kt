package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalTime

data class ScheduleRequest(
    @SerializedName("routine") val routine: List<Schedule>
)

data class Schedule(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("weekday") val weekday: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
)
