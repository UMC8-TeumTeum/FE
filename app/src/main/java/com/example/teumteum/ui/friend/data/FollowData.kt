package com.example.teumteum.ui.friend.data

data class FollowData(
    val userId: Int,
    val name: String,
    val profileImageUrl: String,
    val field: String,
    val following: Boolean,
    var favorite: Boolean
)
