package com.umc.teumteum.data.remote.auth.repository

import android.util.Log
import com.umc.teumteum.data.remote.auth.model.SocialLoginRequest
import com.umc.teumteum.data.remote.auth.model.SocialNonceLoginRequest
import com.umc.teumteum.data.remote.auth.service.AuthService
import com.umc.teumteum.ui.auth.data.SocialLoginResult
import com.umc.teumteum.ui.auth.data.SocialProvider
import com.umc.teumteum.utils.handleApiResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthService
) {
    suspend fun loginWithSocialAccessToken(provider: SocialProvider, token: String): Result<SocialLoginResult> = runCatching {
        val response = authService.socialLogin(
            socialType = provider.socialType,
            body = SocialLoginRequest(token = token)
        )
        Log.d("SocialLogin", "response = ${response.body()}")
        handleApiResponse(response)
    }

    suspend fun loginWithIdTokenAndNonce(provider: SocialProvider, token: String, nonce: String): Result<SocialLoginResult> = runCatching {
        val response = authService.socialLogin(
            socialType = provider.socialType,
            body = SocialNonceLoginRequest(token = token, nonce = nonce)
        )
        Log.d("SocialLogin", "response = ${response.body()}")
        handleApiResponse(response)
    }
}