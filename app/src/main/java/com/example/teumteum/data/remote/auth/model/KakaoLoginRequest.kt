package com.example.teumteum.data.remote.auth.signin.model

import com.google.gson.annotations.SerializedName

data class KakaoSignInRequest(
    @SerializedName("accessToken") val accessToken: String
)
