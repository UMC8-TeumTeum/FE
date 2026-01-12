package com.example.teumteum.data.remote.auth.model

import com.google.gson.annotations.SerializedName

data class SocialNonceLoginRequest(
    @SerializedName("token") val token: String,
    @SerializedName("nonce") val nonce: String
)