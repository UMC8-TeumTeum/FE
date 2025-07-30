package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName

data class NicknameJobRequest(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("jobField") val jobField: String
)