package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class PossibleTimeRequest(
    @SerializedName("userIds") val userIds: List<Int>,
    @SerializedName("date") val date: String
)
