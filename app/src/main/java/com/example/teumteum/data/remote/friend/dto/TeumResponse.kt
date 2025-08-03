package com.example.teumteum.data.remote.friend.dto

import com.google.gson.annotations.SerializedName

// 추후에 필요 틈 요청하기 api 연결 시
data class TeumResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: TeumRequestResult
)

data class TeumRequestResult(
    @SerializedName("id") val id: Int
)


