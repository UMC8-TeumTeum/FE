package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class TeumStatusResult(
    @SerializedName("status") val status: String,        // "ACCEPTED" or "REJECTED"
    @SerializedName("teumCreated") val teumCreated: Boolean,  // ACCEPTED인 경우 true
    @SerializedName("teumId") val teumId: Int?           // ACCEPTED이면 ID, REJECTED면 null
)
