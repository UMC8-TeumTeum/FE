package com.umc.teumteum.data.remote.alarm

data class PushAlarmItem(
    val id: Int,
    val title: String,
    val content: String?,
    val timeAgo: String
)