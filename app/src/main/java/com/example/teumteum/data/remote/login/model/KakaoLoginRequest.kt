package com.example.teumteum.data.remote.login.model

import com.google.gson.annotations.SerializedName

data class KakaoLoginRequest(
    @SerializedName("accessToken") val accessToken: String
)
