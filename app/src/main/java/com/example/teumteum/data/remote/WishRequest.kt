package com.example.teumteum.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterWishRequest(
    @SerializedName(value = "title") val title: String,
    @SerializedName(value = "content") val content: String,
    @SerializedName(value = "estimatedDuration") val estimatedDuration: String,
    @SerializedName(value = "categories") val categories: List<Long>
)