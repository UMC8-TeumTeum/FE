package com.umc.teumteum.data.remote.friend.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class TeumConflictResponse(
    @SerializedName("hasConflict") val hasConflict: Boolean,
    @SerializedName("conflictingRequests") val conflictingRequests: List<TeumConflictItem>
)

@Parcelize
data class TeumConflictItem(
    @SerializedName("id") val id: Int,
    @SerializedName("receiverNickname") val receiverNickname: String,
    @SerializedName("receiverProfileImageUrl") val receiverProfileImageUrl: String?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
) : Parcelable

