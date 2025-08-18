package com.example.teumteum.data.remote.alarm

import android.util.Log
import com.example.teumteum.data.remote.alarm.dto.FcmToken
import com.example.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRepository @Inject constructor(
    private val fcmService: FcmService,
    private val fcmTokenStore: FcmTokenStore,
) {
    suspend fun registerToken(token: String): Result<Unit> = runCatching {
        require(token.isNotBlank()) { "Empty FCM token" }
        val response = fcmService.registerToken(FcmToken(token))
        handleApiResponseUnit(response)
    }

    suspend fun deactivateCurrentDeviceToken(): Result<Unit> = runCatching {
        val token = fcmTokenStore.load().orEmpty()
        if (token.isBlank()) {
            return@runCatching Unit
        }

        val response = fcmService.deactivateToken(FcmToken(token))
        try {
            handleApiResponseUnit(response)
        } catch (e: Exception) {
            // 로그만 남기고 계속 진행
            val code = (response.body()?.code ?: "HTTP${response.code()}")
            when (code) {
                "FCM4002" -> Log.i("FcmRepository", "Token already deactivated; proceed")
                "FCM4001" -> Log.w("FcmRepository", "Token not found for user; proceed")
                "COMMON400" -> Log.w("FcmRepository", "Bad request for deactivation; proceed")
                "COMMON401" -> Log.w("FcmRepository", "Auth required/expired; proceed")
                else -> Log.e("FcmRepository", "Deactivate failed: $code", e)
            }
        }

        // 로컬 토큰도 비워 재사용 방지
        fcmTokenStore.clear()
    }
}
