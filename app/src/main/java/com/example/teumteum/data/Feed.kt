package com.example.teumteum.data

data class Feed(
    val username: String,
    val userField: String,
    val title: String,
    val time: Int,
    val contents: String,
    val borderTitle: String,
    val borderLink: String,
    val isBookMarked: Boolean,
    val isFromFollowedUser: Boolean
)