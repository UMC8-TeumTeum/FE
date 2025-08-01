package com.example.teumteum.data.remote.activity.dto

import com.example.teumteum.data.entities.EstimatedDurationType
import com.google.gson.annotations.SerializedName

data class ActivityWishRequest(
    @SerializedName("estimatedDuration") val estimatedDuration: EstimatedDurationType,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("customCategory") val customCategory: String? = null
)

data class ActivityAiRequest(
    @SerializedName("estimatedDuration") val estimatedDuration: EstimatedDurationType,
    @SerializedName("location") val location: String? = null,
    @SerializedName("categoryId") val categoryId: Long? = null
)