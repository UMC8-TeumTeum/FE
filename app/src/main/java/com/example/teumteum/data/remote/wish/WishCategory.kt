package com.example.teumteum.data.remote.wish

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)
