package com.umc.teumteum.data.remote.mypage.service

import com.umc.teumteum.data.remote.mypage.model.AlarmSettingResponse
import com.umc.teumteum.data.remote.mypage.model.PushAlarmRequest
import com.umc.teumteum.data.remote.mypage.model.RemindAlarmRequest
import com.umc.teumteum.data.remote.mypage.model.RemindAlarmResponse
import com.umc.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.umc.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

interface SettingService {
    @GET("/api/users/mypage/reminders")
    suspend fun getRemindAlarms(): Response<ApiResponse<RemindAlarmResponse>>

    @PATCH("/api/users/mypage/reminders")
    suspend fun updateRemindAlarms(@Body remindAlarms: RemindAlarmRequest): Response<ApiResponse<Unit>>

    @PATCH("/api/users/mypage/alarm")
    suspend fun updatePushAlarms(@Body pushAlarms: PushAlarmRequest): Response<ApiResponse<Unit>>

    @PATCH("/api/users/mypage/sleep-pattern")
    suspend fun updateSleepPattern(@Body sleepPattern: SleepPatternRequest): Response<ApiResponse<Unit>>

    @DELETE("/api/users/mypage/sleep-pattern")
    suspend fun deleteSleepPattern(): Response<ApiResponse<Unit>>

    @GET("/api/users/mypage/alarm")
    suspend fun getAlarmSettings(): Response<ApiResponse<AlarmSettingResponse>>

}