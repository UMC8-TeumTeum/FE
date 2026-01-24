package com.umc.teumteum.data.remote.activity.model

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

data class AssignWishRequest(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
)

data class AssignAiRequest(
    @SerializedName("id") val id: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
)