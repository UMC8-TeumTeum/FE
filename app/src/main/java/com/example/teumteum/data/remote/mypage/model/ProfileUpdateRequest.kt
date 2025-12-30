package com.example.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("jobField") val jobField: String,
    @SerializedName("timePublic") val timePublic: Boolean
)