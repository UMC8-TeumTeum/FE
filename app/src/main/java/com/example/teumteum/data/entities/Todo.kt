package com.example.teumteum.data.entities

import com.google.gson.annotations.SerializedName

data class Todo(
    @SerializedName(value = "title") val title: String,
    @SerializedName(value = "startTime") val startTime: String,
    @SerializedName(value = "endTime") val endTime: String,
    @SerializedName(value = "description") val description: String,
    @SerializedName(value = "isPublic") val isPublic: Boolean,
    @SerializedName(value = "includeTeum") val includeTeum: Boolean,
    @SerializedName(value = "remindAlarm") val remindAlarm: List<Int>? = null
)