package com.example.teumteum.data.remote.activity.repository

import android.util.Log
import com.example.teumteum.data.remote.activity.model.ActivityWishRequest
import com.example.teumteum.data.remote.activity.model.ActivityWishResultWrapper
import com.example.teumteum.data.remote.activity.service.ActivityService
import com.example.teumteum.utils.ApiResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityService: ActivityService
) {
    // 채움활동 위시리스트 불러오기
    suspend fun activityWish(request: ActivityWishRequest): Result<ApiResponse<ActivityWishResultWrapper>> {
        return try {
            val response = activityService.activityWish(request)
            Log.d("ActivityWish", "response = ${response.body()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                if (apiResponse.isSuccess && apiResponse.result != null) {
                    Result.success(apiResponse)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }
}