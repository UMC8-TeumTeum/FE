package com.example.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class SleepPatternResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
)
