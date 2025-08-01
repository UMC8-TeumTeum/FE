package com.example.teumteum.data.remote.home.dto

import com.example.teumteum.data.TimeType
import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<ScheduleResult>
)

data class ScheduleResult(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("type") val type: TimeType,
)
