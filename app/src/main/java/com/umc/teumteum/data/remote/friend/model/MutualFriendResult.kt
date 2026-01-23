package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class MutualFriendResult(
    val content: List<MutualFriendItem>,
    val hasNext: Boolean
)

data class MutualFriendItem(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String
)

