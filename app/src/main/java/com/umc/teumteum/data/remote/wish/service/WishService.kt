package com.umc.teumteum.data.remote.wish.service

import com.umc.teumteum.data.remote.activity.model.AssignWishRequest
import com.umc.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.umc.teumteum.data.remote.wish.model.EditWishRequest
import com.umc.teumteum.data.remote.wish.model.RegisterWishRequest
import com.umc.teumteum.data.remote.wish.model.WishCategories
import com.umc.teumteum.data.remote.wish.model.WishResult
import com.umc.teumteum.data.remote.wish.model.WishlistResult
import com.umc.teumteum.utils.ApiResponse
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
    suspend fun registerWish(@Body request: RegisterWishRequest): Response<ApiResponse<Unit>>

    @GET("/api/wishes/wishlist")
    suspend fun getWishlist(@Query("duration") duration: String, @Query("page") page: Int): Response<ApiResponse<WishlistResult>>

    @GET("/api/wishes/{wishId}")
    suspend fun getWish(@Path("wishId") wishId: Long): Response<ApiResponse<WishResult>>

    @PATCH("/api/wishes/{wishId}")
    suspend fun editWish(@Path("wishId") wishId: Long, @Body request: EditWishRequest): Response<ApiResponse<Unit>>

    @HTTP(method = "DELETE", path = "/api/wishes", hasBody = true)
    suspend fun deleteWishes(@Body request: DeleteWishesRequest): Response<ApiResponse<Unit>>

    @POST("/api/wishes/{wishId}/assign")
    suspend fun assignWish(@Path("wishId") wishId: Long, @Body request: AssignWishRequest): Response<ApiResponse<Unit>>
}