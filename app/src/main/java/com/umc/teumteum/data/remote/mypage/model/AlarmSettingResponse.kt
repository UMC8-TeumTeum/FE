package com.umc.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class AlarmSettingResponse(
    @SerializedName("todayTodo") val todayTodo: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: Boolean,
    @SerializedName("follow") val follow: Boolean,
    @SerializedName("teum") val teum: Boolean,
)
