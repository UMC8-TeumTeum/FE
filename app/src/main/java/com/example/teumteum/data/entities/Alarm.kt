package com.example.teumteum.data.entities

data class AlarmItem(
    val id: Int,
    val friendNickname: String,
    val friendProfileImage: String,
    val content: String,
    val elapsedTime: String,
    var isRead: Boolean = false
)

data class PushAlarmItem(
    val id: Int,
    val type: String,
    val body: String,
    val elapsedTime: String,
    val isRead: Boolean = false
)