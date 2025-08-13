package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class TeumTimeResult(
    @SerializedName("days") val days: Int,
    @SerializedName("hours") val hours: Int,
    @SerializedName("minutes") val minutes: Int,
    @SerializedName("totalMinutes") val totalMinutes: Int
)
