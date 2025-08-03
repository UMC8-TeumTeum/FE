package com.example.teumteum.data.remote.activity.model

import com.example.teumteum.data.remote.todo.model.TodoResult
import com.google.gson.annotations.SerializedName

data class ActivityWishResponse(
    @SerializedName("result") val result: ActivityWishResultWrapper?
)

data class ActivityWishResultWrapper(
    @SerializedName("wishes") val wishes: List<ActivityWishResult>?
)

data class ActivityAiResponse(
    @SerializedName("aiContents") val aiContents: List<ActivityAiResult>?
)

data class FillAiResponse(
    @SerializedName("result") val result: TodoResult?
)