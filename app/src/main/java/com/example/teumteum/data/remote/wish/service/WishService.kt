package com.example.teumteum.data.remote.wish.service

import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.DeleteWishesResponse
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.EditWishResponse
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.RegisterWishResponse
import com.example.teumteum.data.remote.wish.model.WishlistResult
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WishService {

    @POST("/api/wishes")
    suspend fun registerWish(@Body request: RegisterWishRequest): Response<ApiResponse<RegisterWishResponse>>

    @GET("/api/wishes/wishlist")
    suspend fun getWishlist(@Query("duration") duration: String, @Query("page") page: Int): Response<ApiResponse<WishlistResult>>

    @GET("/api/wishes/{wishId}")
    suspend fun getWish(@Path("wishId") wishId: Long): Response<ApiResponse<Wish>>

    @PATCH("/api/wishes/{wishId}")
    suspend fun editWish(@Path("wishId") wishId: Long, @Body request: EditWishRequest): Response<ApiResponse<EditWishResponse>>

    @HTTP(method = "DELETE", path = "/api/wishes", hasBody = true)
    suspend fun deleteWishes(@Body request: DeleteWishesRequest): Response<ApiResponse<DeleteWishesResponse>>
}