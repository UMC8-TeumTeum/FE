package com.example.teumteum.data.remote.login.service

import com.example.teumteum.data.remote.login.model.JwtTokenResponse
import com.example.teumteum.data.remote.login.model.KakaoLoginRequest
import com.example.teumteum.data.remote.login.model.ReissueRequest
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("api/auth/social-login/kakao")
    suspend fun loginWithKakao( @Body request: KakaoLoginRequest ): Response<ApiResponse<JwtTokenResponse>>

    @POST("/api/auth/reissue")
    suspend fun reissue( @Body request: ReissueRequest ): Response<ApiResponse<JwtTokenResponse>>

}