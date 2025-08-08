package com.example.teumteum.data.remote.login.model

import com.google.gson.annotations.SerializedName

data class JwtTokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)