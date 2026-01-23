package com.umc.teumteum.data.remote.mypage.model

import com.google.gson.annotations.SerializedName

data class PushAlarmRequest(
    @SerializedName("todayTodo") val todayTodo: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: Boolean,
    @SerializedName("teum") val teum: Boolean,
    @SerializedName("follow") val follow: Boolean
)
