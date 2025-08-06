package com.example.teumteum.data.remote.login.repository

import com.example.teumteum.data.remote.login.model.JwtTokenResponse
import com.example.teumteum.data.remote.login.model.KakaoLoginRequest
import com.example.teumteum.data.remote.login.service.AuthService
import com.example.teumteum.utils.handleApiResponse

import javax.inject.Inject

class LoginRepository @Inject constructor(

    private val authService: AuthService
) {
    suspend fun loginWithKakaoAccessToken(token: String): Result<JwtTokenResponse> = runCatching {
        val response = authService.loginWithKakao(KakaoLoginRequest(token))
        handleApiResponse(response)
    }
}
