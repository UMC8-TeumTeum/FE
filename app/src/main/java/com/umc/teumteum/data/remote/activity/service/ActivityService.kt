package com.umc.teumteum.data.remote.activity.service

import com.umc.teumteum.data.remote.activity.model.ActivityAiRequest
import com.umc.teumteum.data.remote.activity.model.ActivityAiResponse
import com.umc.teumteum.data.remote.activity.model.ActivityWishRequest
import com.umc.teumteum.data.remote.activity.model.ActivityWishResponse
import com.umc.teumteum.data.remote.activity.model.AssignAiRequest
import com.umc.teumteum.data.remote.activity.model.AssignWishRequest
import com.umc.teumteum.utils.ApiResponse
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

    @POST("/api/activities/ai/assign")
    suspend fun assignAi(@Body request: AssignAiRequest): Response<ApiResponse<Unit>>
}