package com.example.teumteum.data.remote.calendar.dto

import com.google.gson.annotations.SerializedName

data class CalendarResult(
    @SerializedName("date") val date: String,
    @SerializedName("hasSchedule") val hasSchedule: Boolean
)