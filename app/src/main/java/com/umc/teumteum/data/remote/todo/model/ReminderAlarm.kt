package com.umc.teumteum.data.remote.todo.model

import com.umc.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.google.gson.annotations.SerializedName

data class ReminderAlarm(
    @SerializedName("alarm") var alarm: Int,
    @SerializedName("status") var status: AlarmStatus
)