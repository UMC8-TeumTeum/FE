package com.example.teumteum.data.remote.onboarding.service

import com.example.teumteum.data.remote.onboarding.model.RemindRequest
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OnBoardingService {

    @POST("/api/users/onboarding/reminders")
    suspend fun postRemind(@Body request: RemindRequest): Response<ApiResponse<Unit>>
}