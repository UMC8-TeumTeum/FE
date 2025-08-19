package com.example.teumteum.data.remote.alarm.dto.enums

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    @SerializedName("TEUM_REQUEST") TEUM_REQUEST,
    @SerializedName("TEUM_ACCEPTED") TEUM_ACCEPTED,
    @SerializedName("TEUM_DECLINED") TEUM_DECLINED,
    @SerializedName("TEUM_SUGGESTED") TEUM_SUGGESTED,
    @SerializedName("TEUM_CANCELED") TEUM_CANCELED,
    @SerializedName("FOLLOW") FOLLOW
}