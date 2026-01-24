package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class TeumRequestDateResult(
    @SerializedName("requestId") val requestId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String, // "YYYY-MM-DD"
    @SerializedName("timeSlot") val timeSlot: TimeSlotDto,
    @SerializedName("requester") val requester: UserMiniDto,
    @SerializedName("pending") val pending: List<UserMiniDto>,
    @SerializedName("accepted") val accepted: List<UserMiniDto>,
    @SerializedName("cancelled") val cancelled: List<UserMiniDto>,
    @SerializedName("resend") val resend: List<UserMiniDto>,
    @SerializedName("isResend") val isResend: Boolean,
    @SerializedName("isCancelled") val isCancelled: Boolean
)

// 시간 범위 DTO
data class TimeSlotDto(
    @SerializedName("start") val start: String, // "HH:mm"
    @SerializedName("end") val end: String      // "HH:mm"
)

// 간단한 사용자 정보 DTO
data class UserMiniDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)

