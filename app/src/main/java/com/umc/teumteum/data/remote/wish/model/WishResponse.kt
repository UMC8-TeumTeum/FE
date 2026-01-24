package com.umc.teumteum.data.remote.wish.model

import com.umc.teumteum.data.remote.todo.model.TodoResult
import com.google.gson.annotations.SerializedName

data class WishlistResult(
    @SerializedName("wishlist") val wishlist: List<WishlistItem>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("isFirst") val isFirst: Boolean,
    @SerializedName("isLast") val isLast: Boolean
)

data class WishResult(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("categories") val categories: List<WishCategories>
)

data class FillWishResponse(
    @SerializedName("result") val result: TodoResult?
)

data class WishCategories(
    @SerializedName("id") val categoryId: Long,
    @SerializedName("name") val categoryName: String
)