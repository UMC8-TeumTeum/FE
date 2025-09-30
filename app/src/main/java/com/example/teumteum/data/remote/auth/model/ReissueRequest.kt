package com.example.teumteum.data.remote.auth.signin.model

import com.google.gson.annotations.SerializedName

data class ReissueRequest (
    @SerializedName("refreshToken") val refreshToken: String
)
