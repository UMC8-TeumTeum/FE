package com.umc.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class MySocialInfoResponse(
    @SerializedName("email") val email: String,
    @SerializedName("socialType") val socialType: String
)
