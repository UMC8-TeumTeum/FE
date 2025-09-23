package com.example.teumteum.data.remote.calendar.model

import com.google.gson.annotations.SerializedName

data class GetCalendarResponse(
    @SerializedName("date") val date: String,
    @SerializedName("hasSchedule") var hasSchedule: Boolean
)