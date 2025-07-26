package com.example.teumteum.data.entities

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)
