package com.umc.teumteum.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.umc.teumteum.R
import com.umc.teumteum.ui.main.MainActivity
import com.umc.teumteum.ui.auth.LoginActivity
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.utils.FlowPrefs
import com.umc.teumteum.utils.NextStep
import com.umc.teumteum.utils.TokenProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject lateinit var tokenProvider: TokenProvider
    @Inject lateinit var flowPrefs: FlowPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // 2초 딜레이 후 라우팅
        Handler(Looper.getMainLooper()).postDelayed({
            routeFromSplash()
        }, 2000)
    }

    // 토큰 + 마지막 단계 기준으로 진입 경로 결정
    private fun routeFromSplash() {
        val raw = tokenProvider.getAccessToken()
        val hasAccess = !raw.isNullOrBlank()
        val last = flowPrefs.getLastStep()

        val target = when {
            !hasAccess -> {
                Intent(this, LoginActivity::class.java)
            }
            last == NextStep.AGREEMENT || last == NextStep.ONBOARDING -> {
                Intent(this, SignUpActivity::class.java)
            }
            last == NextStep.MAIN -> {
                Intent(this, MainActivity::class.java)
            }
            else -> {
                Intent(this, LoginActivity::class.java)
            }
        }

        startActivity(target)
        finish()
    }
}
