package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName

data class PresignedRequest(
    @SerializedName("contentType") val contentType: String
)