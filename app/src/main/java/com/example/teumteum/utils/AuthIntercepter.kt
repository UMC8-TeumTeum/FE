package com.example.teumteum.utils

import android.util.Log
import com.example.teumteum.BuildConfig
import com.example.teumteum.data.remote.login.LogoutManager
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
        // RetryOnceTag를 붙여 무한 루프 및 중복 재발급 방지
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
                // 재발급 실패 시에는 현재 응답을 그대로 반환
                return response
            }
        }

        // 401이면 RT 만료/무효로 간주하고 즉시 로그아웃
        // 재발급 API에서 401이 떨어지는 경우는 RT 만료 외에는 거의 없으므로 세션을 종료
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
            val source = response.body?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer?.clone()
            val responseBodyString = buffer?.readUtf8() ?: ""
            if (responseBodyString.isNotEmpty()) {
                val errorResponse = Gson().fromJson(responseBodyString, ErrorResponse::class.java)
                errorResponse.code == JWT_EXPIRED_CODE
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error parsing response body", e)
            true
        }
    }

    // 에러코드만 뽑아보기 (재발급 로직과 분리)
    private fun extractErrorCode(response: Response): String? = try {
        val source = response.body?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer?.clone()
        val bodyString = buffer?.readUtf8() ?: ""
        if (bodyString.isNotEmpty()) {
            Gson().fromJson(bodyString, ErrorResponse::class.java)?.code
        } else null
    } catch (e: Exception) {
        Log.e("AuthInterceptor", "Error parsing code", e); null
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
                    val errCode = try {
                        if (!errBody.isNullOrEmpty())
                            Gson().fromJson(errBody, ErrorResponse::class.java)?.code
                        else null
                    } catch (_: Exception) { null }

                    Log.e(
                        "AuthInterceptor",
                        "Token refresh failed: 401${if (errCode != null) " (code=$errCode)" else ""}. Forcing logout."
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
