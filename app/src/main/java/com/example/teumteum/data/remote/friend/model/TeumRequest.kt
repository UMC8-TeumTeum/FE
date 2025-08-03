package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

// 추후에 필요 틈 요청하기 api 연결 시
data class TeumRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String, // "YYYY-MM-DD"
    @SerializedName("startTime") val startTime: String, // "HH:MM"
    @SerializedName("endTime") val endTime: String,   // "HH:MM"
    @SerializedName("graphicId") val graphicId: Int,
    @SerializedName("receiverUserIds") val receiverUserIds: List<Int>
)
