package com.example.teumteum.data.remote.auth.onboarding.model

import com.google.gson.annotations.SerializedName

data class ProfileImageRequest(
    @SerializedName("fileName") val fileName: String
)
