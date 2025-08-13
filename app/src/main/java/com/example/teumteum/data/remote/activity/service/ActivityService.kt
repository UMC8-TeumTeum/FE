package com.example.teumteum.data.remote.activity.service

import com.example.teumteum.data.remote.activity.model.ActivityAiRequest
import com.example.teumteum.data.remote.activity.model.ActivityAiResponse
import com.example.teumteum.data.remote.activity.model.ActivityWishRequest
import com.example.teumteum.data.remote.activity.model.ActivityWishResponse
import com.example.teumteum.data.remote.activity.model.AssignWishRequest
import com.example.teumteum.data.remote.activity.model.FillAiRequest
import com.example.teumteum.data.remote.activity.model.FillAiResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ActivityService {
    @POST("/api/activities/user-wishes")
    suspend fun activityWish(@Body request: ActivityWishRequest): Response<ApiResponse<ActivityWishResponse>>

    @POST("/api/activities/ai")
    suspend fun activityAi(@Body request: ActivityAiRequest): Response<ApiResponse<ActivityAiResponse>>

    @POST("/api/wishes/{wishId}/assign")
    suspend fun assignWish(@Path("wishId") wishId: Long, @Body request: AssignWishRequest): Response<ApiResponse<Unit>>

    @POST("/api/activies/ai/assign")
    suspend fun fillAi(@Path("aiContentId") aiContentId: Long, @Body request: FillAiRequest): Call<FillAiResponse>
}