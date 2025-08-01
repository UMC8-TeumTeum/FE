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

data class FillWishRequest(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isForce") val isForce: Boolean? = null
)