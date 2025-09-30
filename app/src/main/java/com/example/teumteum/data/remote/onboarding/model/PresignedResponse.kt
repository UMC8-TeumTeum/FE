package com.example.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class PresignedResponse(
    @SerializedName("presignedUrl") val presignedUrl: String,
    @SerializedName("fileName") val fileName: String
)


