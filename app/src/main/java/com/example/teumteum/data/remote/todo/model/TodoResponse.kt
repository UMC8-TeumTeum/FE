package com.example.teumteum.data.remote.todo.model

import com.google.gson.annotations.SerializedName

data class RegisterTodoResponse(
    @SerializedName("result") val result: TodoResult?
)

data class GetTodoListResponse(
    @SerializedName("result") val result: TodoListResult?
)

data class GetTodoResponse(
    @SerializedName("result") val result: GetTodoResult?
)

data class EditTodoResponse(
    @SerializedName("result") val result: TodoResult?
)

data class DeleteTodoResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
)