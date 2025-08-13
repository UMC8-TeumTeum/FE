package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class ResendTeumRequest(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)
