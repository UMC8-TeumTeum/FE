package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class PublicTodoResult(
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String, // "HH:MM"
    @SerializedName("endTime") val endTime: String      // "HH:MM"
)
