package com.example.teumteum.data.remote.wish.dto

import com.google.gson.annotations.SerializedName

data class WishlistItem (
    @SerializedName(value = "id") val id: Long,
    @SerializedName(value = "title") val title: String,
    @SerializedName(value = "estimatedDuration") val estimatedDuration: String
)