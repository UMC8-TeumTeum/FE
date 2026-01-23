package com.umc.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class NicknameJobResponse(
    @SerializedName("isSuccess") val isSuccess: String,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
)
