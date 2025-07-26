package com.example.teumteum.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterWishResponse(
    @SerializedName(value = "isSuccess") val isSuccess: Boolean,
    @SerializedName(value = "code") val code: String,
    @SerializedName(value = "message") val message: String,
    @SerializedName("result") val result: Map<String, String>?
)

data class GetWishlistResponse(
    @SerializedName(value = "isSuccess") val isSuccess: Boolean,
    @SerializedName(value = "code") val code: String,
    @SerializedName(value = "message") val message: String,
    @SerializedName("result") val result: WishlistResult?
)