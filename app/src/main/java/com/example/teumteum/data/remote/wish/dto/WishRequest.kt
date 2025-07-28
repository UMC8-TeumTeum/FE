package com.example.teumteum.data.remote.wish.dto

import com.google.gson.annotations.SerializedName

data class RegisterWishRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("categories") val categories: List<Long>
)

data class EditWishRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("categories") val categories: List<Long>
)

data class DeleteWishesRequest(
    @SerializedName("wishIds") val wishIds: List<Long>
)