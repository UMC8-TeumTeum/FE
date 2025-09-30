package com.example.teumteum.data.remote.onboarding.repository

import android.util.Log
import com.example.teumteum.data.remote.onboarding.model.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedResponse
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.model.RemindRequest
import com.example.teumteum.data.remote.onboarding.service.OnBoardingService
import com.example.teumteum.utils.handleApiResponse
import com.example.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnBoardingRepository @Inject constructor(
    private val onBoardingService: OnBoardingService
){

    // 닉네임, 직종 입력
    suspend fun postNicknameAndJobField(request: NicknameJobRequest): Result<Unit> = runCatching {
        val response = onBoardingService.postNicknameAndJobField(request)
        Log.d("NickNameAndJob", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // 수면패턴 등록
    suspend fun postSleepPattern(request: SleepPatternRequest): Result<Unit> = runCatching {
        val response = onBoardingService.postSleepPattern(request)
        Log.d("SleepPattern", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // 반복일정 등록
    suspend fun postSchedules(request: ScheduleRequest): Result<Unit> = runCatching {
        val response = onBoardingService.postSchedules(request)
        Log.d("Schedules", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //프리사인드 url 발급 요청
    suspend fun requestPresignedUrl(request: PresignedRequest): Result<PresignedResponse> = runCatching {
        val response = onBoardingService.requestPresignedUrl(request)
        Log.d("PresignedUrl", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //이미지 등록
    suspend fun postProfileImage(request: ProfileImageRequest): Result<Unit> = runCatching {
        val response = onBoardingService.postProfileImage(request)
        Log.d("ProfileImage", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    //리마인드 알림 등록
    suspend fun postRemind(request: RemindRequest): Result<Unit> = runCatching {
        val response = onBoardingService.postRemind(request)
        Log.d("Remind", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }
}