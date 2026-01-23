package com.umc.teumteum.data.remote.activity.repository

import android.util.Log
import com.umc.teumteum.data.remote.activity.model.ActivityAiRequest
import com.umc.teumteum.data.remote.activity.model.ActivityAiResponse
import com.umc.teumteum.data.remote.activity.model.ActivityWishRequest
import com.umc.teumteum.data.remote.activity.model.ActivityWishResponse
import com.umc.teumteum.data.remote.activity.model.AssignAiRequest
import com.umc.teumteum.data.remote.activity.model.AssignWishRequest
import com.umc.teumteum.data.remote.activity.service.ActivityService
import com.umc.teumteum.utils.handleApiResponse
import com.umc.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityService: ActivityService
) {
    // 채움활동 위시리스트 불러오기
    suspend fun activityWish(request: ActivityWishRequest): Result<ActivityWishResponse> = runCatching {
        val response = activityService.activityWish(request)
        Log.d("ActivityWish", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 채움활동 ai컨텐츠 불러오기
    suspend fun activityAi(request: ActivityAiRequest): Result<ActivityAiResponse> = runCatching {
        val response = activityService.activityAi(request)
        Log.d("ActivityAi", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 위시 빈틈 채우기
    suspend fun assignWish(wishId: Long, request: AssignWishRequest): Result<Unit> = runCatching {
        val response = activityService.assignWish(wishId, request)
        Log.d("AssignWish", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // ai컨텐츠 빈틈 채우기
    suspend fun assignAi(request: AssignAiRequest): Result<Unit> = runCatching {
        val response = activityService.assignAi(request)
        Log.d("AssignAi", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }
}