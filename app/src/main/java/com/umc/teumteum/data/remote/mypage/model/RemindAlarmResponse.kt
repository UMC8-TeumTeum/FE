package com.umc.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class RemindAlarmResponse(
    @SerializedName("remindAlarms") val remindAlarms: List<Int>
)
