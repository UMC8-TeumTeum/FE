package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName

data class PresignedResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: PresignedFileInfo
)

data class PresignedFileInfo (
    @SerializedName("presignedUrl") val presignedUrl: String,
    @SerializedName("fileName") val fileName: String
)

