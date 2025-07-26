package com.example.teumteum.data.entities

import com.example.teumteum.data.remote.wish.dto.WishCategory
import com.google.gson.annotations.SerializedName

data class WishItem(
    val id: Int,
    val title: String,
    val time: String,
    val category: String,
    var isChecked: Boolean = false
)

data class Wish(
    @SerializedName(value = "id") val id: Long,
    @SerializedName(value = "title") val title: String,
    @SerializedName(value = "estimatedDuration") val estimatedDuration: String,
    @SerializedName(value = "categories") val categories: List<WishCategory>
)