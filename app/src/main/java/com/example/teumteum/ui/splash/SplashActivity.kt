package com.example.teumteum.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.teumteum.R
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signin.LoginActivity
import com.example.teumteum.ui.signup.SignUpActivity
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.NextStep
import com.example.teumteum.utils.TokenProvider
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
