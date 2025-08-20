package com.example.teumteum.utils

import android.util.Log
import com.example.teumteum.BuildConfig
import com.example.teumteum.data.remote.login.LogoutManager
import com.example.teumteum.data.remote.login.model.ReissueRequest
import com.example.teumteum.data.remote.login.service.AuthService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

// 요청 단위 1회 재시도 가드용 마커
private object RetryOnceTag

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val logoutManager: LogoutManager
) : Interceptor {

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val JWT_EXPIRED_CODE = "AUTH4113"
        private const val HTTP_UNAUTHORIZED = 401

        // 재발급 대상 제외, 즉시 로그아웃할 코드들
        private val FORCE_LOGOUT_CODES = setOf(
            "AUTH4101", // 잘못된 형식
            "AUTH4102", // 미지원 형식
            "AUTH4103", // 비어있는 클레임
            "AUTH4112", // 잘못된 서명
            "AUTH4114", // AT 대신 RT 사용
            "AUTH4115", // 블랙리스트 AT
            "AUTH4131", // 비활성 사용자(탈퇴 등)
            "USER4001", // 유저 없음
            "COMMON401" // 기타 인증 필요
        )

        private val NO_AUTH_PATHS = listOf(
            "/api/auth/social-login/kakao",
            "/api/auth/reissue"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 이미 재발급 재시도를 했는지(tag) 확인
        val hasRetried = originalRequest.tag(RetryOnceTag::class.java) != null
        if (isNoAuthRequired(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenProvider.getAccessToken()
        val refreshToken = tokenProvider.getRefreshToken()

        val requestWithToken = originalRequest.newBuilder().apply {
            if (!accessToken.isNullOrEmpty()) {
                addHeader(AUTH_HEADER, "$BEARER_PREFIX$accessToken")
            }
        }.build()

        val response = chain.proceed(requestWithToken)

        // 이미 재시도한 요청은 더 이상 재발급을 시도하지 않음
        if (!hasRetried && isTokenExpired(response) && !refreshToken.isNullOrEmpty()) {
            response.close()

            val newAccessToken = refreshAccessToken(refreshToken)
            if (!newAccessToken.isNullOrEmpty()) {
                val newRequest = originalRequest.newBuilder()
                    .header(AUTH_HEADER, "$BEARER_PREFIX$newAccessToken")
                    .tag(RetryOnceTag::class.java, RetryOnceTag)
                    .build()
                return chain.proceed(newRequest)
            } else {
                // 재발급 실패 시에는 기존 응답을 그대로 반환
                return response
            }
        }

        // 401이면 RT 만료/무효로 간주하고 즉시 로그아웃 (만료 코드 제외 + 강제 로그아웃 코드만)
        if (response.code == HTTP_UNAUTHORIZED) {
            val code = extractErrorCode(response)
            if (code != null && code != JWT_EXPIRED_CODE && FORCE_LOGOUT_CODES.contains(code)) {
                logoutManager.forceLogout()
            }
        }

        return response
    }

    private fun isNoAuthRequired(path: String): Boolean {
        return NO_AUTH_PATHS.any { path.contains(it) }
    }

    // 401 + 본문코드가 AUTH4113(또는 바디 없음/파싱실패) → 만료로 간주
    private fun isTokenExpired(response: Response): Boolean {
        if (response.code != HTTP_UNAUTHORIZED) return false
        return try {
            val bodyString = readBodyString(response)
            if (bodyString.isNotEmpty()) {
                val api = parseApiErrorBody(bodyString)
                api?.code == JWT_EXPIRED_CODE
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error parsing response body", e)
            true
        }
    }

    // 에러코드만 추출 (재발급 로직과 분리)
    private fun extractErrorCode(response: Response): String? = try {
        val bodyString = readBodyString(response)
        if (bodyString.isNotEmpty()) parseApiErrorBody(bodyString)?.code else null
    } catch (e: Exception) {
        Log.e("AuthInterceptor", "Error parsing code", e)
        null
    }

    // peekBody로 본문을 소비하지 않고 미리보기 문자열 획득
    private fun readBodyString(response: Response): String {
        return try {
            response.peekBody(1024 * 1024).string() // 1MB
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error peeking body", e)
            ""
        }
    }

    // ApiResponse<Any?>로 에러 바디 파싱
    private fun parseApiErrorBody(bodyString: String): ApiResponse<Any?>? = try {
        val type = object : TypeToken<ApiResponse<Any?>>() {}.type
        Gson().fromJson<ApiResponse<Any?>>(bodyString, type)
    } catch (_: Exception) {
        null
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        // 동시 재발급을 1회로 합쳐줌
        return RefreshSingleFlight.refreshBlocking {
            reissueOnce(refreshToken)
        }
    }

    // 네트워크 호출만 담당 (싱글플라이트 블록 안에서 호출)
    private suspend fun reissueOnce(refreshToken: String): String? {
        return try {
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
                // 401이면 RT 만료/무효로 간주하고 즉시 로그아웃
                if (reissueResponse.code() == HTTP_UNAUTHORIZED) {
                    val errBody = reissueResponse.errorBody()?.string()
                    val errCode = errBody?.let { parseApiErrorBody(it)?.code }

                    Log.e(
                        "AuthInterceptor",
                        "Token refresh failed: 401${if (errCode != null) " (code=$errCode)" else ""} Forcing logout"
                    )
                    logoutManager.forceLogout()
                } else {
                    Log.e("AuthInterceptor", "Token refresh failed: ${reissueResponse.code()}")
                }
                null
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Token refresh error", e)
            null
        }
    }
}
