package com.example.teumteum.utils

import android.util.Log
import com.example.teumteum.data.remote.alarm.FcmRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutUseCase @Inject constructor(
    private val logoutManager: LogoutManager,
    private val fcmRepository: FcmRepository,
) {
    companion object {
        private const val TAG = "LogoutUseCase"
    }

    suspend fun deactivateFcmAndLogout() {
        // 1) FCM 토큰 비활성화 (실패해도 로그만 남기고 진행)
        fcmRepository
            .deactivateCurrentDeviceToken()
            .onFailure { Log.w(TAG, "FCM deactivate failed; continue logout", it) }

        // 2) 서버 로그아웃 + 로컬 정리 + 화면 전환
        logoutManager.logout()
    }
}
