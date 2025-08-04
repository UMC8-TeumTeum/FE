package com.example.teumteum.data.remote.onboarding

import com.example.teumteum.data.remote.onboarding.model.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.model.NicknameJobResponse
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedResponse
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.model.ProfileImageResponse
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.model.SleepPatternResponse
import com.example.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.model.ScheduleResponse
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