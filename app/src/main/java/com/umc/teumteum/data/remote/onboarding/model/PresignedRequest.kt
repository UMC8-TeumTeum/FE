package com.umc.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class PresignedRequest(
    @SerializedName("contentType") val contentType: String
)