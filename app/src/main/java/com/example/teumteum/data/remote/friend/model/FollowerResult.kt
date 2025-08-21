package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class FollowerResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("job") val job: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String
)