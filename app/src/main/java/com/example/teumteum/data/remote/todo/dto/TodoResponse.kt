package com.example.teumteum.data.remote.todo.dto

import com.example.teumteum.data.entities.TodoList
import com.google.gson.annotations.SerializedName

data class RegisterTodoResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: TodoResult?
)

data class GetTodoListResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: TodoListResult?
)