package com.example.teumteum.data.remote.auth.model

import com.google.gson.annotations.SerializedName

data class KakaoLoginRequest(
    @SerializedName("token") val token: String
)
