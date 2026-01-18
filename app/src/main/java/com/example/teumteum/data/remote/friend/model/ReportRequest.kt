package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("targetType") val targetType: String,
    @SerializedName("targetId") val targetId: Long,
    @SerializedName("reasonId") val reasonId: Int,
    @SerializedName("otherReason") val otherReason: String?
)