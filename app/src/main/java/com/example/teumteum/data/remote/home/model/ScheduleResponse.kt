package com.example.teumteum.data.remote.home.model

import com.example.teumteum.ui.main.data.TimeType
import com.google.gson.annotations.SerializedName

data class ScheduleResult(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("type") val type: TimeType,
)
