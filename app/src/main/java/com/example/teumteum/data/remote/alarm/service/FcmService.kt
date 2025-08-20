package com.example.teumteum.data.remote.alarm.service

import com.example.teumteum.data.remote.alarm.dto.FcmToken
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface FcmService {

    // 로그인 직후: FCM 토큰 등록
    @POST("/api/fcm/token")
    suspend fun registerToken(
        @Body body: FcmToken
    ): Response<ApiResponse<Unit>>

    // 로그아웃 직전: FCM 토큰 비활성화
    @PATCH("/api/fcm/token")
    suspend fun deactivateToken(
        @Body body: FcmToken
    ): Response<ApiResponse<Unit>>

}