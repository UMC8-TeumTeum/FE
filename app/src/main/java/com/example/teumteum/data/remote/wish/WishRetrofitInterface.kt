package com.example.teumteum.data.remote.wish

import com.example.teumteum.data.remote.wish.dto.GetWishResponse
import com.example.teumteum.data.remote.wish.dto.GetWishlistResponse
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.dto.RegisterWishResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WishRetrofitInterface {

    @POST("/api/wishes")
    fun wishRegister(@Body request: RegisterWishRequest): Call<RegisterWishResponse>

    @GET("/api/wishes/wishlist")
    fun getWishlist(@Query("duration") duration: String, @Query("page") page: Int): Call<GetWishlistResponse>

    @GET("/api/wishes/{wishId}")
    fun getWish(@Path("wishId") wishId: Long): Call<GetWishResponse>
}