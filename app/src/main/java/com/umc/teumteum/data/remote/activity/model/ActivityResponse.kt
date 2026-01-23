package com.umc.teumteum.data.remote.activity.model

import com.google.gson.annotations.SerializedName

data class ActivityWishResponse(
    @SerializedName("wishes") val wishes: List<ActivityWishResult>?
)

data class ActivityWishResult(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String
)

data class ActivityAiResponse(
    @SerializedName("aiContents") val aiContents: List<ActivityAiResult>?
)

data class ActivityAiResult(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String
)