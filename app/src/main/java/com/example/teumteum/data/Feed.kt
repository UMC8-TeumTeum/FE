package com.example.teumteum.data

data class Feed(
    val id: Int,
    val username: String,
    val content: String,
    val createdAt: Long,
    val isFromFollowedUser: Boolean
)