package com.example.teumteum.data.remote.friend.dto

import com.google.gson.annotations.SerializedName

data class TeumRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String, // "YYYY-MM-DD"
    @SerializedName("startTime") val startTime: String, // "HH:MM"
    @SerializedName("endTime") val endTime: String,   // "HH:MM"
    @SerializedName("graphicId") val graphicId: Int,
    @SerializedName("receiverUserIds") val receiverUserIds: List<Int>
)
