package com.umc.teumteum.data.remote.auth.model

import com.google.gson.annotations.SerializedName

data class JwtTokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)