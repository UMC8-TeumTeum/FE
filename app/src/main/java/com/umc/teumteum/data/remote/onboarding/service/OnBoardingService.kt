package com.umc.teumteum.data.remote.onboarding.service

import com.umc.teumteum.data.remote.onboarding.model.AgreementRequest
import com.umc.teumteum.data.remote.onboarding.model.NicknameJobRequest
import com.umc.teumteum.data.remote.onboarding.model.PresignedRequest
import com.umc.teumteum.data.remote.onboarding.model.PresignedResponse
import com.umc.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.umc.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.umc.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.umc.teumteum.data.remote.onboarding.model.RemindRequest
import com.umc.teumteum.utils.ApiResponse
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

    @POST("/api/users/onboarding/reminders")
    suspend fun postRemind(@Body request: RemindRequest): Response<ApiResponse<Unit>>
}