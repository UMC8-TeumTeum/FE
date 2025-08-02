package com.example.teumteum.data.remote.friend.dto

import com.google.gson.annotations.SerializedName

data class TeumResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: TeumRequestResult
)

data class TeumRequestResult(
    @SerializedName("id") val id: Int
)


