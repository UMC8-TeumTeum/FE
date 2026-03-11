package com.umc.teumteum.data.remote.alarm.model

data class NotificationsPage(
    val content: List<NotificationResponse>,
    val hasNext: Boolean,
    val currentPage: Int, // 1부터 시작
    val size: Int
)