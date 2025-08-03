package com.example.teumteum.data.remote.wish.model

import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.remote.todo.model.TodoResult
import com.google.gson.annotations.SerializedName

data class RegisterWishResponse(
    @SerializedName("result") val result: Map<String, String>? = null
)

data class GetWishlistResponse(
    @SerializedName("result") val result: WishlistResult?
)

data class GetWishResponse(
    @SerializedName("result") val result: Wish
)

data class EditWishResponse(
    @SerializedName("result") val result: Map<String, String>?
)

data class DeleteWishesResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String
)

data class FillWishResponse(
    @SerializedName("result") val result: TodoResult?
)