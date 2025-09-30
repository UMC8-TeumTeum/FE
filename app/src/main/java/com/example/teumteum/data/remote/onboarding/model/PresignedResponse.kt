package com.example.teumteum.data.remote.auth.onboarding.model

import com.google.gson.annotations.SerializedName

data class PresignedResponse(
    @SerializedName("presignedUrl") val presignedUrl: String,
    @SerializedName("fileName") val fileName: String
)


