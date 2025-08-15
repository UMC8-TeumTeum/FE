package com.example.teumteum.data.remote.login.repository

import android.util.Log
import com.example.teumteum.data.remote.login.model.KakaoLoginRequest
import com.example.teumteum.data.remote.login.service.AuthService
import com.example.teumteum.ui.signin.data.SocialLoginResult
import com.example.teumteum.utils.handleApiResponse

import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val authService: AuthService
) {
    suspend fun loginWithKakaoAccessToken(token: String): Result<SocialLoginResult> = runCatching {
        val response = authService.loginWithKakao(KakaoLoginRequest(token))
        Log.d("KakaoLogin", "response = ${response.body()}")
        handleApiResponse(response)
    }
}