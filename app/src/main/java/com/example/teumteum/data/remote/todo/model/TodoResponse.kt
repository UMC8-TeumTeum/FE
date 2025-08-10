package com.example.teumteum.data.remote.todo.model

import com.example.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.google.gson.annotations.SerializedName

data class TodoResult(
    @SerializedName("todoId") val todoId: Long
)

data class TodoListResult(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("hasAlarm") var hasAlarm: Boolean? = null,
    @SerializedName("alarmStatus") var alarmStatus: AlarmStatus,
    @SerializedName("type") val type: ScheduleType
)

data class GetTodoResult(
    @SerializedName("type") val type: ScheduleType,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("includeTeum") val includeTeum: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: List<Int>? = null,
    @SerializedName("profileUrl") val profileUrl: List<String>? = null
)

data class GetOnboardingReminders(
    @SerializedName("reminders") val reminders: List<Int>
)