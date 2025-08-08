package com.example.teumteum.data.entities

import com.example.teumteum.data.entities.enums.TodoType
import com.google.gson.annotations.SerializedName

data class Todo(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("includeTeum") val includeTeum: Boolean,
    @SerializedName("remindAlarm") val remindAlarm: List<Int>? = null
)

data class TodoHomeItem(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val isPublic: Boolean,
    var isAlarmOn: Boolean? = null
)

data class TodoList(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("hasAlarm") var hasAlarm: Boolean? = null,
    @SerializedName("type") val type: TodoType
)