package com.example.teumteum.data.remote.activity.dto

import com.example.teumteum.data.remote.todo.dto.TodoResult
import com.google.gson.annotations.SerializedName

data class ActivityWishResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: ActivityWishResultWrapper?
)

data class ActivityWishResultWrapper(
    @SerializedName("wishes") val wishes: List<ActivityWishResult>?
)

data class ActivityAiResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("aiContents") val aiContents: List<ActivityAiResult>?
)

data class FillAiResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: TodoResult?
)