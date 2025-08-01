package com.example.teumteum.data.remote.activity

import com.example.teumteum.data.remote.activity.dto.ActivityAiRequest
import com.example.teumteum.data.remote.activity.dto.ActivityAiResponse
import com.example.teumteum.data.remote.activity.dto.ActivityWishRequest
import com.example.teumteum.data.remote.activity.dto.ActivityWishResponse
import com.example.teumteum.data.remote.activity.dto.FillAiRequest
import com.example.teumteum.data.remote.activity.dto.FillAiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ActivityRetrofitInterface {

    @POST("/api/activities/user-wishes")
    fun activityWish(@Body request: ActivityWishRequest): Call<ActivityWishResponse>

    @POST("/api/activities/ai")
    fun activityAi(@Body request: ActivityAiRequest): Call<ActivityAiResponse>

    @POST("/api/activies/ai/assign")
    fun fillAi(@Path("aiContentId") aiContentId: Long, @Body request: FillAiRequest): Call<FillAiResponse>
}