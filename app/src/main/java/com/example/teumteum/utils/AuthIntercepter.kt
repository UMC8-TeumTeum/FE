package com.example.teumteum.utils

import android.util.Log
import com.example.teumteum.BuildConfig
import com.example.teumteum.data.remote.login.model.ReissueRequest
import com.example.teumteum.data.remote.login.service.AuthService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

data class ErrorResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val JWT_EXPIRED_CODE = "AUTH4113"
        private const val HTTP_UNAUTHORIZED = 401

        private val NO_AUTH_PATHS = listOf(
            "/api/auth/social-login/kakao",
            "/api/auth/reissue"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 토큰이 필요 없는 요청들은 바로 진행
        if (isNoAuthRequired(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenProvider.getAccessToken()
        val refreshToken = tokenProvider.getRefreshToken()

        // Access Token으로 요청 생성
        val requestWithToken = originalRequest.newBuilder().apply {
            if (!accessToken.isNullOrEmpty()) {
                addHeader(AUTH_HEADER, "$BEARER_PREFIX$accessToken")
            }
        }.build()

        val response = chain.proceed(requestWithToken)

        // JWT 토큰 만료 확인 (HTTP 401 + 응답 본문의 코드 확인)
        if (isTokenExpired(response) && !refreshToken.isNullOrEmpty()) {
            response.close()

            val newAccessToken = refreshAccessToken(refreshToken)

            if (!newAccessToken.isNullOrEmpty()) {
                val newRequest = originalRequest.newBuilder()
                    .header(AUTH_HEADER, "$BEARER_PREFIX$newAccessToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun isNoAuthRequired(path: String): Boolean {
        return NO_AUTH_PATHS.any { path.contains(it) }
    }

    private fun isTokenExpired(response: Response): Boolean {
        if (response.code != HTTP_UNAUTHORIZED) return false

        return try {
            val source = response.body?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer?.clone()
            val responseBodyString = buffer?.readUtf8() ?: ""

            if (responseBodyString.isNotEmpty()) {
                val errorResponse = Gson().fromJson(responseBodyString, ErrorResponse::class.java)
                errorResponse.code == JWT_EXPIRED_CODE
            } else {
                // 응답 본문이 없으면 401만으로 판단
                true
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error parsing response body", e)
            // JSON 파싱 실패 시에도 401이면 토큰 만료로 처리
            true
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return runBlocking {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val tempAuthService = retrofit.create(AuthService::class.java)
                val reissueResponse = tempAuthService.reissue(ReissueRequest(refreshToken))

                if (reissueResponse.isSuccessful) {
                    val newToken = reissueResponse.body()?.result
                    fun String.mask() = if (length > 10) take(4) + "..." + takeLast(6) else "***"

                    Log.d("AuthInterceptor", "Token refreshed successfully")
                    Log.d("AuthInterceptor", "NewAccessToken: ${newToken?.accessToken?.mask()}")
                    Log.d("AuthInterceptor", "NewRefreshToken: ${newToken?.refreshToken?.mask()}")

                    tokenProvider.saveTokens(
                        accessToken = newToken?.accessToken ?: "",
                        refreshToken = newToken?.refreshToken ?: refreshToken
                    )
                    newToken?.accessToken
                } else {
                    Log.e("AuthInterceptor", "Token refresh failed: ${reissueResponse.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "Token refresh error", e)
                null
            }
        }
    }
}