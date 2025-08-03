package com.example.teumteum.data.remote.wish.model

import com.google.gson.annotations.SerializedName

data class WishlistItem (
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    var isChecked: Boolean = false,
    var isDeleted: Boolean = false
)