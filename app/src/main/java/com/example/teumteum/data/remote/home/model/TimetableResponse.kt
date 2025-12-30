package com.example.teumteum.data.remote.home.model

import com.google.gson.annotations.SerializedName

data class TimetableResponse(
    @SerializedName("sleep") val sleep: List<Time>,
    @SerializedName("todo") val todo: List<Time>
)

data class Time(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)