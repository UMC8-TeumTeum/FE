package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class FollowingPageResult(
    @SerializedName("content") val content: List<FollowingResult>,
    @SerializedName("hasNext") val hasNext: Boolean
)

