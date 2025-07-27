package com.example.teumteum.data.remote.todo.dto

import com.google.gson.annotations.SerializedName

data class TodoResult(
    @SerializedName("todoId") val todoId: Long
)