package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("isSuccess") val isSuccess: String,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
)
