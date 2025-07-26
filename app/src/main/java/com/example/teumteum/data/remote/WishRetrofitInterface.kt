package com.example.teumteum.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WishRetrofitInterface {

    @POST("/api/wishes")
    fun wishRegister(@Body request: RegisterWishRequest): Call<RegisterWishResponse>

    @GET("/api/wishes/wishlist")
    fun getWishlist(@Query("duration") duration: String, @Query("page") page: Int): Call<GetWishlistResponse>
}