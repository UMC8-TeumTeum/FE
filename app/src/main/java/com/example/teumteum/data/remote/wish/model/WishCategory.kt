package com.example.teumteum.data.remote.wish.dto

import com.google.gson.annotations.SerializedName

data class WishCategory(
    @SerializedName("id") val categoryId: Long,
    @SerializedName("name") val categoryName: String
)
