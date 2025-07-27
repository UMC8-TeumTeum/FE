package com.example.teumteum.data.remote.wish.dto

import com.google.gson.annotations.SerializedName

data class WishCategory(
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("categoryName") val categoryName: String
)
