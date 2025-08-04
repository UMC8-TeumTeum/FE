package com.example.teumteum.data.remote.onboarding.service

import com.example.teumteum.data.remote.agreement.dto.AgreementResponse
import com.example.teumteum.data.remote.onboarding.model.AgreementRequest
import com.example.teumteum.data.remote.onboarding.model.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.model.NicknameJobResponse
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedResponse
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.model.ProfileImageResponse
import com.example.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.model.ScheduleResponse
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.model.SleepPatternResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OnBoardingService {

    @POST("/api/users/onboarding/agreements")
    suspend fun postAgreements(@Body request: AgreementRequest): Response<ApiResponse<Unit>>

    @POST("/api/users/onboarding/nickname-job")
    suspend fun postNicknameAndJobField(@Body request: NicknameJobRequest): Response<ApiResponse<Unit>>

    @POST("/api/users/onboarding/sleep-pattern")
    suspend fun postSleepPattern(@Body request: SleepPatternRequest): Response<ApiResponse<Unit>>

    @POST("/api/users/onboarding/routines")
    suspend fun postSchedules(@Body request: ScheduleRequest): Response<ApiResponse<Unit>>

    @POST("/api/users/onboarding/profile-image/presigned-url")
    suspend fun requestPresignedUrl(@Body request: PresignedRequest): Response<ApiResponse<PresignedResponse>>

    @POST("/api/users/onboarding/profile-image")
    suspend fun postProfileImage(@Body request: ProfileImageRequest) : Response<ApiResponse<Unit>>

}