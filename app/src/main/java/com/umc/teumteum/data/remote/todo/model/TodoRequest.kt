package com.umc.teumteum.data.remote.todo.model

import com.umc.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.google.gson.annotations.SerializedName

data class RegisterTodoRequest(
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("includeTeum") val includeTeum: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: List<ReminderAlarm>? = null
)

data class EditTodoRequest(
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("includeTeum") val includeTeum: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: List<ReminderAlarm>? = null
)

data class AlarmStatusRequest(
    @SerializedName("todoId") val todoId: Long,
    @SerializedName("alarmStatus") var alarmStatus: AlarmStatus
)