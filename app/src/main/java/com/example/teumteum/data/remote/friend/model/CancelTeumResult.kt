package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class CancelTeumResult(
    @SerializedName("cancelledUserIds") val cancelledUserIds: List<Int>
)
