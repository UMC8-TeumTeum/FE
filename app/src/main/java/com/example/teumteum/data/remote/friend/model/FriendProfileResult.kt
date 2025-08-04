package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class FriendProfileResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("field") val field: String,
    @SerializedName("following") val following: Boolean,
    @SerializedName("favorite") val favorite: Boolean
)
