package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class FriendSearchResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("job") val job: String
)

