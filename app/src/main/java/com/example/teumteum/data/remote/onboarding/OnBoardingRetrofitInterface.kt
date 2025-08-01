package com.example.teumteum.data.remote.onboarding

import com.example.teumteum.data.remote.onboarding.dto.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobResponse
import com.example.teumteum.data.remote.onboarding.dto.PresignedRequest
import com.example.teumteum.data.remote.onboarding.dto.PresignedResponse
import com.example.teumteum.data.remote.onboarding.dto.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.dto.ProfileImageResponse
import com.example.teumteum.data.remote.onboarding.dto.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.dto.SleepPatternResponse
import com.example.teumteum.data.remote.onboarding.dto.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.dto.ScheduleResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OnBoardingRetrofitInterface {
    @POST("/api/users/onboarding/nickname-job")
    fun postNicknameAndJobField(@Body request: NicknameJobRequest): Call<NicknameJobResponse>

    @POST("/api/users/onboarding/sleep-pattern")
    fun postSleepPattern(@Body request: SleepPatternRequest): Call<SleepPatternResponse>

    @POST("/api/users/onboarding/routines")
    fun postSchedules(@Body request: ScheduleRequest): Call<ScheduleResponse>

    @POST("/api/users/onboarding/profile-image/presigned-url")
    fun requestPresignedUrl(@Body request: PresignedRequest): Call<PresignedResponse>

    @POST("/api/users/onboarding/profile-image")
    fun postProfileImage(@Body request: ProfileImageRequest) : Call<ProfileImageResponse>
}