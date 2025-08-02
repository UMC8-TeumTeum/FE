package com.example.teumteum.data.remote.wish

import com.example.teumteum.data.remote.wish.dto.FillWishResponse
import com.example.teumteum.data.remote.wish.dto.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.dto.DeleteWishesResponse
import com.example.teumteum.data.remote.wish.dto.EditWishRequest
import com.example.teumteum.data.remote.wish.dto.EditWishResponse
import com.example.teumteum.data.remote.wish.dto.FillWishRequest
import com.example.teumteum.data.remote.wish.dto.GetWishResponse
import com.example.teumteum.data.remote.wish.dto.GetWishlistResponse
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.dto.RegisterWishResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WishService {

    @POST("/api/wishes")
    suspend fun registerWish(@Body request: RegisterWishRequest): Call<RegisterWishResponse>

    @GET("/api/wishes/wishlist")
    suspend fun getWishlist(@Query("duration") duration: String, @Query("page") page: Int): Call<GetWishlistResponse>

    @GET("/api/wishes/{wishId}")
    suspend fun getWish(@Path("wishId") wishId: Long): Call<GetWishResponse>

    @PATCH("/api/wishes/{wishId}")
    suspend fun editWish(@Path("wishId") wishId: Long, @Body request: EditWishRequest): Call<EditWishResponse>

    @HTTP(method = "DELETE", path = "/api/wishes", hasBody = true)
    suspend fun deleteWishes(@Body request: DeleteWishesRequest): Call<DeleteWishesResponse>

    @POST("/api/wishes/{wishId}/assign")
    suspend fun fillWish(@Path("wishId") wishId: Long, @Body request: FillWishRequest): Call<FillWishResponse>
}