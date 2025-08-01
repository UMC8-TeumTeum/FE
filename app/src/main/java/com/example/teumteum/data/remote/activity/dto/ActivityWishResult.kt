package com.example.teumteum.data.remote.activity.dto

import com.example.teumteum.data.entities.enums.EstimatedDurationType
import com.google.gson.annotations.SerializedName

data class ActivityWishResult(
    @SerializedName("id") val id: Long,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: EstimatedDurationType
)