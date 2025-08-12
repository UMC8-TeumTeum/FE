package com.example.teumteum.data.remote.activity.model

import com.google.gson.annotations.SerializedName

data class ActivityWishRequest(
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("customCategory") val customCategory: String? = null
)

data class ActivityAiRequest(
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("locationId") val locationId: Long? = null,
    @SerializedName("customLocation") val customLocation: String? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("customCategory") val customCategory: String? = null
)

data class FillAiRequest(
    @SerializedName("aiContentId") val aiContentId: Long,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isForce") val isForce: Boolean? = null
)