package com.example.teumteum.data.entities

import com.example.teumteum.data.remote.wish.model.WishCategories
import com.google.gson.annotations.SerializedName

data class Wish(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("estimatedDuration") val estimatedDuration: String,
    @SerializedName("categories") val categories: List<WishCategories>
)