package com.example.teumteum.data.remote.friend.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FriendProfileResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("field") val field: String,
    @SerializedName("following") val following: Boolean,
    @SerializedName("favorite") val favorite: Boolean
) : Parcelable
