package com.example.teumteum.data.entities

import com.google.gson.annotations.SerializedName

data class WishlistResult (
    @SerializedName("wishlist") val wishlist: List<WishlistItem>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("isFirst") val isFirst: Boolean,
    @SerializedName("isLast") val isLast: Boolean
)