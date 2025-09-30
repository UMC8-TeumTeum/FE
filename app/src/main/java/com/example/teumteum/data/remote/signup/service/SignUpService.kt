package com.example.teumteum.data.remote.signup.service

import com.example.teumteum.data.remote.signup.model.AgreementRequest
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpService {
    @POST("/api/users/onboarding/agreements")
    suspend fun postAgreements(@Body request: AgreementRequest): Response<ApiResponse<Unit>>
}