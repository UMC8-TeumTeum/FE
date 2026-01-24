package com.umc.teumteum.data.remote.friend.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class TodoConflictResponse(
    @SerializedName("hasConflict") val hasConflict: Boolean,
    @SerializedName("conflictingSchedules") val conflictingSchedules: List<TodoConflictItem>
)

@Parcelize
data class TodoConflictItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
) : Parcelable