package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class SharedTeumListResult(
    @SerializedName("content") val content: List<SharedTeumItem>,
    @SerializedName("hasNext") val hasNext: Boolean
)

data class SharedTeumItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: SharedTeumTime,
    @SerializedName("sender") val sender: SharedTeumSender,
    @SerializedName("isSender") val isSender: Boolean
)

data class SharedTeumTime(
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String
)

data class SharedTeumSender(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String
)

