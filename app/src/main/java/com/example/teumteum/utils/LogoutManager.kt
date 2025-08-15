package com.example.teumteum.utils

import android.content.Context
import android.content.Intent
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.teumteum.ui.signin.LoginActivity

@Singleton
class LogoutManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenProvider: TokenProvider,
    private val flowPrefs: FlowPrefs
) {
    private val loggingOut = AtomicBoolean(false)

    fun forceLogout() {
        // 중복 호출 방지
        if (!loggingOut.compareAndSet(false, true)) return

        // 로컬 상태 초기화 -> 추후에 로그아웃 API 호출로 변경 예정
        tokenProvider.clearTokens()
        flowPrefs.clear()

        // 로그인 화면으로 전환
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        loggingOut.set(false)
    }
}
