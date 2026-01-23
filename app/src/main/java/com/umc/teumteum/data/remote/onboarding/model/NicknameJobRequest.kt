package com.umc.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class NicknameJobRequest(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("jobField") val jobField: String
)