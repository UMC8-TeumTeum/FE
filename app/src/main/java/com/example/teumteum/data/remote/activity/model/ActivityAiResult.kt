package com.example.teumteum.data.remote.activity.dto

import com.google.gson.annotations.SerializedName

data class ActivityAiResult(
    @SerializedName("aiContentsId") val aiContentsId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String
)
