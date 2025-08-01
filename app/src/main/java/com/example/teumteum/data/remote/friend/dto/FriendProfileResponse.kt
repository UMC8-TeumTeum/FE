package com.example.teumteum.data.remote.friend.dto

import com.google.gson.annotations.SerializedName

data class FriendProfileResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: FriendProfileResult?
)

data class FriendProfileResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("field") val field: String,
    @SerializedName("following") val following: Boolean,
    @SerializedName("favorite") val favorite: Boolean
)
