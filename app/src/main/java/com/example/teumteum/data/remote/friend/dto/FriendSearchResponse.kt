package com.example.teumteum.data.remote.friend.dto

import com.google.gson.annotations.SerializedName

data class FriendSearchResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<FriendSearchResult>?
)

data class FriendSearchResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("job") val job: String
)

