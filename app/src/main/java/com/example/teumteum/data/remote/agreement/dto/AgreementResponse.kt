package com.example.teumteum.data.remote.agreement.dto

import com.google.gson.annotations.SerializedName

data class AgreementResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
)
