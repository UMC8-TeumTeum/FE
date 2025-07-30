package com.example.teumteum.ui.friend.adapter

data class TeumRequestItem(
    val name: String,
    val date: String,
    val time: String,
    val title: String,
    val description: String,
    val profileImageRes: Int,
    val isCanceled: Boolean = false
)

