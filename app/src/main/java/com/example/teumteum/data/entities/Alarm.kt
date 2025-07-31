package com.example.teumteum.data.entities

import com.example.teumteum.data.remote.alarm.dto.AlarmType
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class AlarmItem(
    val id: Int,
    val friendNickname: String,
    val friendProfileImage: String,
    val content: String,
    val elapsedTime: String,
    var isRead: Boolean = false
)

data class Alarm(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: AlarmType,
    @SerializedName("relatedId") val relatedId: Long,
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("createdAt") val createdAt: LocalDateTime,
    @SerializedName("friendId") val friendId: Long,
    @SerializedName("friendNickname") val friendNickname: String,
    @SerializedName("friendProfileImage") val friendProfileImage: String
)