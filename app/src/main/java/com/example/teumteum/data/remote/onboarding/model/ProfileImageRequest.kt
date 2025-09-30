package com.example.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class ProfileImageRequest(
    @SerializedName("fileName") val fileName: String
)
