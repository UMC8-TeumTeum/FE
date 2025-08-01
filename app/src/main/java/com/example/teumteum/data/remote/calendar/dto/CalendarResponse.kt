package com.example.teumteum.data.remote.calendar.dto

import com.google.gson.annotations.SerializedName

data class CalendarResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<CalendarResult>?
)