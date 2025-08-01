package com.example.teumteum.data.remote.alarm.dto

import com.google.gson.annotations.SerializedName

data class GetAlarmListResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: AlarmListResult?
)
