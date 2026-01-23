package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class TeumScheduleDetailResult(
    @SerializedName("teumId") val teumId: Int,
//    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String, // "YYYY-MM-DD"
    @SerializedName("startTime") val startTime: String, // "HH:MM"
    @SerializedName("endTime") val endTime: String,     // "HH:MM"
    @SerializedName("status") val status: String,
    @SerializedName("participants") val participants: List<TeumParticipant>
)

data class TeumParticipant(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String
)
