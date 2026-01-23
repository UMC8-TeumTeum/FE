package com.umc.teumteum.data.remote.alarm.dto

import com.umc.teumteum.data.remote.alarm.dto.enums.NotificationType
import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: NotificationType,
    @SerializedName("relatedId") val relatedId: Int,
    @SerializedName("content") val content: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("friendId") val friendId: Int,
    @SerializedName("friendNickname") val friendNickname: String,
    // 서버에서 오타(firendProfileImage)로 내려오므로 둘 다 매핑
    @SerializedName("friendProfileImage") val friendProfileImage: String? = null,
    @SerializedName("firendProfileImage") val firendProfileImage: String? = null
) {
    fun getProfileImageUrl(): String? {
        return friendProfileImage ?: firendProfileImage
    }
}