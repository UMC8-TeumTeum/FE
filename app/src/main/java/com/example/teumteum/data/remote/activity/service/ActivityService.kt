package com.example.teumteum.data.remote.activity.service

import com.example.teumteum.data.remote.activity.model.ActivityAiRequest
import com.example.teumteum.data.remote.activity.model.ActivityAiResponse
import com.example.teumteum.data.remote.activity.model.ActivityWishRequest
import com.example.teumteum.data.remote.activity.model.ActivityWishResultWrapper
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
    suspend fun activityWish(@Body request: ActivityWishRequest): Response<ApiResponse<ActivityWishResultWrapper>>

    @POST("/api/activities/ai")
    suspend fun activityAi(@Body request: ActivityAiRequest): Call<ActivityAiResponse>

    @POST("/api/activies/ai/assign")
    suspend fun fillAi(@Path("aiContentId") aiContentId: Long, @Body request: FillAiRequest): Call<FillAiResponse>
}