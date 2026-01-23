package com.umc.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class FollowingResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("job") val job: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("isFavorite") val isFavorite: Boolean
)
