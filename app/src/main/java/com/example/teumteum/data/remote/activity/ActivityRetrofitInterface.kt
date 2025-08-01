package com.example.teumteum.data.remote.activity

import com.example.teumteum.data.remote.activity.dto.ActivityAiRequest
import com.example.teumteum.data.remote.activity.dto.ActivityAiResponse
import com.example.teumteum.data.remote.activity.dto.ActivityWishRequest
import com.example.teumteum.data.remote.activity.dto.ActivityWishResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ActivityRetrofitInterface {

    @POST("/api/activities/user-wishes")
    fun activityWish(@Body request: ActivityWishRequest): Call<ActivityWishResponse>

    @POST("/api/activities/ai")
    fun activityAi(@Body request: ActivityAiRequest): Call<ActivityAiResponse>
}