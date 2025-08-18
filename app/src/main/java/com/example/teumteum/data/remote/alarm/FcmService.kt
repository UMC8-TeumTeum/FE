package com.example.teumteum.data.remote.alarm

import com.example.teumteum.data.remote.alarm.dto.FcmToken
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmService {

    // 로그인 직후: FCM 토큰 등록
    @POST("/api/fcm/token")
    suspend fun registerToken(
        @Body body: FcmToken
    ): Response<ApiResponse<Unit>>

}