package com.example.teumteum.data.entities

import com.google.gson.annotations.SerializedName

data class WishlistItem (
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String
)