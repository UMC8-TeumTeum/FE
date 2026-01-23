package com.umc.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class MyInfoResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("job") val field: String
)