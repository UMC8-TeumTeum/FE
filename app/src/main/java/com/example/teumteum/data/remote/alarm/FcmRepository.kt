package com.example.teumteum.data.remote.alarm

import android.util.Log
import com.example.teumteum.data.remote.alarm.dto.FcmToken
import com.example.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRepository @Inject constructor(
    private val fcmService: FcmService
) {
    suspend fun registerToken(token: String): Result<Unit> = runCatching {
        require(token.isNotBlank()) { "Empty FCM token" }
        val response = fcmService.registerToken(FcmToken(token))
        handleApiResponseUnit(response)
        Log.d("FcmRepository", "registerToken success")
    }

}
