package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName

data class ProfileImageRequest(
    @SerializedName("fileName") val fileName: String
)
