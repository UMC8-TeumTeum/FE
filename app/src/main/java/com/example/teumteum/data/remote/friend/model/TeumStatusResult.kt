package com.example.teumteum.data.remote.friend.model

data class TeumStatusResult(
    val status: String,        // "ACCEPTED" or "REJECTED"
    val teumCreated: Boolean,  // ACCEPTED인 경우 true
    val teumId: Int?           // ACCEPTED이면 ID, REJECTED면 null
)
