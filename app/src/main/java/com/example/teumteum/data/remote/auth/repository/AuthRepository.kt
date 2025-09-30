package com.example.teumteum.data.remote.auth.signin.repository

import android.util.Log
import com.example.teumteum.data.remote.auth.signin.model.KakaoSignInRequest
import com.example.teumteum.data.remote.auth.signin.service.AuthService
import com.example.teumteum.ui.auth.signin.data.SocialLoginResult
import com.example.teumteum.utils.handleApiResponse

import javax.inject.Inject

class SignInRepository @Inject constructor(
    private val authService: AuthService
) {
    suspend fun loginWithKakaoAccessToken(token: String): Result<SocialLoginResult> = runCatching {
        val response = authService.loginWithKakao(KakaoSignInRequest(token))
        Log.d("KakaoLogin", "response = ${response.body()}")
        handleApiResponse(response)
    }
}