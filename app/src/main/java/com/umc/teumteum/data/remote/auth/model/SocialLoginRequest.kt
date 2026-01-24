package com.umc.teumteum.data.remote.auth.model

import com.google.gson.annotations.SerializedName

data class SocialLoginRequest(
    @SerializedName("token") val token: String
)
