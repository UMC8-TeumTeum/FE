package com.example.teumteum.data.remote.mypage.repository

import android.util.Log
import com.example.teumteum.data.remote.mypage.model.PushAlarmRequest
import com.example.teumteum.data.remote.mypage.model.RemindAlarmRequest
import com.example.teumteum.data.remote.mypage.model.RemindAlarmResponse
import com.example.teumteum.data.remote.mypage.service.SettingService
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.utils.handleApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository @Inject constructor(
    private val settingService: SettingService
) {
    //리마인드 알림 조회
    suspend fun getRemindAlarms(): Result<RemindAlarmResponse> = runCatching {
        val response = settingService.getRemindAlarms()
        Log.d("Setting", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //리마인드 알림 수정
    suspend fun updateRemindAlarms(remindAlarms: RemindAlarmRequest): Result<Unit> = runCatching {
        val response = settingService.updateRemindAlarms(remindAlarms)
        Log.d("Setting", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //알림 수정
    suspend fun updatePushAlarms(pushAlarms: PushAlarmRequest): Result<Unit> = runCatching {
        val response = settingService.updatePushAlarms(pushAlarms)
        Log.d("Setting", "response = ${response.body()}")
        handleApiResponse(response)
    }

    //수면패턴 수정
    suspend fun updateSleepPattern(sleepPattern: SleepPatternRequest): Result<Unit> = runCatching {
        val response = settingService.updateSleepPattern(sleepPattern)
        Log.d("Setting", "response = ${response.body()}")
        handleApiResponse(response)
    }
}