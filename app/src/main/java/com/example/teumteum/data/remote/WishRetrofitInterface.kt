package com.example.teumteum.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface WishRetrofitInterface {
    @POST("/api/wishes")
    fun wishRegister(@Body request: WishRegisterRequest): Call<WishRegisterResponse>
}