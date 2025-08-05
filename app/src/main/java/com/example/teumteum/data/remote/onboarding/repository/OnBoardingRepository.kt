package com.example.teumteum.data.remote.onboarding.repository

import android.util.Log
import com.example.teumteum.data.remote.onboarding.model.RemindRequest
import com.example.teumteum.data.remote.onboarding.service.OnBoardingService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnBoardingRepository @Inject constructor(
    private val onBoardingService: OnBoardingService
){
    //리마인드 알림 등록
    suspend fun postRemind(request: RemindRequest): Result<Unit> = runCatching {
        val response = onBoardingService.postRemind(request)
        Log.d("Remind", "response = ${response.body()}")

    }
}