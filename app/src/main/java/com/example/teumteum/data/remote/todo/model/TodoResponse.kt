package com.example.teumteum.data.remote.todo.model

import com.example.teumteum.data.entities.enums.TodoType
import com.google.gson.annotations.SerializedName

data class TodoResult(
    @SerializedName("todoId") val todoId: Long
)

data class GetTodoResult(
    @SerializedName("type") val type: TodoType,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("includeTeum") val includeTeum: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: List<Int>? = null,
    @SerializedName("profileUrl") val profileUrl: List<String>? = null
)