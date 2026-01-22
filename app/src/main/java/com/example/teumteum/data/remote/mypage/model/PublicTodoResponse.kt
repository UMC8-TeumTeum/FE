package com.example.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class PublicTodoResponse(
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)
