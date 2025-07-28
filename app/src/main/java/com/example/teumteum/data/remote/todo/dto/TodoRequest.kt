package com.example.teumteum.data.remote.todo.dto

import com.google.gson.annotations.SerializedName

data class RegisterTodoRequest(
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("includeTeum") val includeTeum: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: List<Int>? = null
)