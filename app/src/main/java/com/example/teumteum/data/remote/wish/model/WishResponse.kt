package com.example.teumteum.data.remote.wish.model

import com.example.teumteum.data.remote.todo.model.TodoResult
import com.google.gson.annotations.SerializedName

data class RegisterWishResponse(
    @SerializedName("result") val result: Map<String, String>? = null
)

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

data class EditWishResponse(
    @SerializedName("result") val result: Map<String, String>?
)

data class FillWishResponse(
    @SerializedName("result") val result: TodoResult?
)

data class WishCategories(
    @SerializedName("id") val categoryId: Long,
    @SerializedName("name") val categoryName: String
)