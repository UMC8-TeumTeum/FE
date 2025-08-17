// utils/LogoutManager.kt
package com.example.teumteum.utils

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.example.teumteum.ui.signin.LoginActivity
import com.example.teumteum.data.remote.login.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import retrofit2.Retrofit
import retrofit2.Response
import javax.inject.Provider

@Singleton
class LogoutManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenProvider: TokenProvider,
    private val flowPrefs: FlowPrefs,
    @AuthRetrofit private val retrofitProvider: Provider<Retrofit>,
) {
    private val loggingOut = AtomicBoolean(false)

    // 앱 전역 스코프
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // AuthService를 지연 생성
    private val authService: AuthService by lazy {
        retrofitProvider.get().create(AuthService::class.java)
    }

    // Interceptor 등 비코루틴 컨텍스트에서 호출 가능
    fun forceLogout() {
        scope.launch {
            logout()
        }
    }

    // 전체 로그아웃 플로우
    suspend fun logout() {
        if (!loggingOut.compareAndSet(false, true)) return
        try {
            // 1) 서버 로그아웃
            runCatching {
                val resp = withContext(Dispatchers.IO) { authService.logout() }
                val body = resp.body()
                val ok = resp.isSuccessful && (body?.isSuccess == true || body?.code == "AUTH2004")
                if (!ok) error(body?.message ?: "Logout failed (${resp.code()})")
            }

            // 2) 로컬 정리
            withContext(Dispatchers.IO) {
                tokenProvider.clearTokens()
                flowPrefs.clear()
            }

            // 3) 화면 전환
            withContext(Dispatchers.Main) {
                val intent = Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
            }
        } finally {
            loggingOut.set(false)
        }
    }
}