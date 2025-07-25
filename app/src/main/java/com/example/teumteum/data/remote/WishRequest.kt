package com.example.teumteum.data.remote

import com.google.gson.annotations.SerializedName

data class WishRegisterRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("categories") val categories: List<Long>
)