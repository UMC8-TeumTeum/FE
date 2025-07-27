package com.example.teumteum.data.remote.wish.dto

import com.google.gson.annotations.SerializedName

data class WishCategory(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)
