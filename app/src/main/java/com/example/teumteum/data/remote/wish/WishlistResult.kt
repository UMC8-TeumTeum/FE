package com.example.teumteum.data.remote

import com.example.teumteum.data.entities.WishlistItem
import com.google.gson.annotations.SerializedName

data class WishlistResult (
    @SerializedName(value = "wishlist") val wishlist: List<WishlistItem>,
    @SerializedName(value = "pageNumber") val pageNumber: Int,
    @SerializedName(value = "pageSize") val pageSize: Int,
    @SerializedName(value = "hasNext") val hasNext: Boolean,
    @SerializedName(value = "isFirst") val isFirst: Boolean,
    @SerializedName(value = "isLast") val isLast: Boolean
)