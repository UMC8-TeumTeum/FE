package com.example.teumteum.data.remote.auth.service

import com.example.teumteum.data.remote.auth.model.JwtTokenResponse
import com.example.teumteum.data.remote.auth.model.ReissueRequest
import com.example.teumteum.data.remote.auth.model.SocialLoginRequest
import com.example.teumteum.ui.auth.data.SocialLoginResult
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {

    @POST("/api/auth/social-login/{socialType}")
    suspend fun socialLogin(
        @Path("socialType") socialType: String,
        @Body body: SocialLoginRequest
    ): Response<ApiResponse<SocialLoginResult>>

    @POST("/api/auth/reissue")
    suspend fun reissue( @Body request: ReissueRequest): Response<ApiResponse<JwtTokenResponse>>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

}