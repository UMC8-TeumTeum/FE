package com.example.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class BlockedUserResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: BlockedUserResult? = null
)

data class BlockedUserResult(
    @SerializedName("content") val content: List<BlockedUser> = emptyList(),
    @SerializedName("hasNext") val hasNext: Boolean
)

data class BlockedUser(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("job") val job: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String
)
