package com.example.teumteum.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teumteum.R
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signin.LoginActivity
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

        Handler(Looper.getMainLooper()).postDelayed({
            val hasAccess = !tokenProvider.getAccessToken().isNullOrBlank()
            val last = flowPrefs.getLastStep()

            val target = if (hasAccess && last == NextStep.MAIN) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(target)
            finish()
        }, 2000)
    }
}
