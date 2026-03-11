package com.umc.teumteum.data.remote.alarm.dto

import com.google.gson.annotations.SerializedName

data class ReadNotificationResult(
    @SerializedName("notificationId") val notificationId: Long,
    @SerializedName("isRead") val isRead: Boolean
)