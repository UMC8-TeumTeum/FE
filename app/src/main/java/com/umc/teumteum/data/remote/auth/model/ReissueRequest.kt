package com.umc.teumteum.data.remote.auth.model

import com.google.gson.annotations.SerializedName

data class ReissueRequest (
    @SerializedName("refreshToken") val refreshToken: String
)
