package com.umc.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class UnBlockResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
)
