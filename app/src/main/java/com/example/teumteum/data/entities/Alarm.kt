package com.example.teumteum.data.entities

data class AlarmItem(
    val id: Int,
    val personName: String,
    val profileUrl: String,
    val title: String,
    val elapsedTime: String,
    val isRead: Boolean = false
)

data class PushAlarmItem(
    val id: Int,
    val type: String,
    val body: String,
    val elapsedTime: String,
    val isRead: Boolean = false
)