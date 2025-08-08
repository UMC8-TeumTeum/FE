package com.example.teumteum.data.remote.home.model

import com.google.gson.annotations.SerializedName

data class TeumTimeResponse(
    @SerializedName("totalMinutes") val totalMinutes: Int,
    @SerializedName("days") val days: Int,
    @SerializedName("hours") val hours: Int,
    @SerializedName("minutes") val minutes: Int
)
