package com.example.teumteum.data.remote.mypage.service

import com.example.teumteum.data.remote.mypage.model.PushAlarmRequest
import com.example.teumteum.data.remote.mypage.model.RemindAlarmRequest
import com.example.teumteum.data.remote.mypage.model.RemindAlarmResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface SettingService {
    @GET("/api/users/mypage/reminders")
    suspend fun getRemindAlarms(): Response<ApiResponse<RemindAlarmResponse>>

    @PATCH("/api/users/mypage/reminders")
    suspend fun updateRemindAlarms(@Body remindAlarms: RemindAlarmRequest): Response<ApiResponse<Unit>>

    @PATCH("/api/users/mypage/alarm")
    suspend fun updatePushAlarms(@Body pushAlarms: PushAlarmRequest): Response<ApiResponse<Unit>>
}