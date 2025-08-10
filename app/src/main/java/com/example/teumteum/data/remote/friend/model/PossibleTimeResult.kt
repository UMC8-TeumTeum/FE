package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class PossibleTimeResult(
    @SerializedName("date") val date: String,
    @SerializedName("availableTime") val availableTime: List<AvailableTime>
)

data class AvailableTime(
    @SerializedName("start") val startTime: String,
    @SerializedName("end") val endTime: String

)
