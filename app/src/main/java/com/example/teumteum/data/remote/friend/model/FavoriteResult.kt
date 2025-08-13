package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class FavoriteResult(
    @SerializedName("userId") val userId: Int,
    @SerializedName("isFavorite") val isFavorite: Boolean
)
