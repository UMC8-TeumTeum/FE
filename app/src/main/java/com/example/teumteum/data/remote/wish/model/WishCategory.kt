package com.example.teumteum.data.remote.wish.model

import com.google.gson.annotations.SerializedName

data class WishCategory(
    @SerializedName("id") val categoryId: Long,
    @SerializedName("name") val categoryName: String
)
