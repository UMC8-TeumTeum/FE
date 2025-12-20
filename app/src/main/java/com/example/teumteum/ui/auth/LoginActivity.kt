package com.example.teumteum.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.teumteum.databinding.ActivityLoginBinding
import com.example.teumteum.ui.auth.data.LoginResult
import com.example.teumteum.ui.auth.data.SocialProvider
import com.example.teumteum.ui.auth.viewModel.LoginViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.NextStep
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    @Inject lateinit var flowPrefs: FlowPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.kakaoLoginBtn.setOnClickListener {
            startKakaoLogin()
        }

        binding.naverLoginBtn.setOnClickListener {
            startNaverLogin()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Loading -> { /* TODO: 로딩 표시 */ }
                is LoginResult.Success -> {
                    // 서버가 알려준 nextStep을 즉시 FlowPrefs에 동기화
                    when (result.nextStep) {
                        NextStep.AGREEMENT, NextStep.ONBOARDING -> {
                            flowPrefs.setLastStep(result.nextStep)
                            startActivity(Intent(this, SignUpActivity::class.java))
                        }
                        NextStep.MAIN -> {
                            flowPrefs.setLastStep(NextStep.MAIN)
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                    finish()
                }
                is LoginResult.Error -> {
                    Log.d("Login", "소셜 로그인 실패 ${result.message}")
                }
            }
        }
    }

    private fun startKakaoLogin() {
        val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                viewModel.onSocialLoginFailed(SocialProvider.KAKAO, error)
            } else if (token != null) {
                viewModel.exchangeKakaoToken(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun startNaverLogin() {
        val callback = object : NidOAuthCallback {

            override fun onSuccess() {
                val accessToken = NidOAuth.getAccessToken()
                Log.d("NAVER", accessToken.toString())

                if (accessToken.isNullOrBlank()) {
                    return
                }

                viewModel.exchangeNaverToken(accessToken)
            }

            override fun onFailure(errorCode: String, errorDesc: String) {
                Log.d("NaverLogin", "네이버 로그인 실패: $errorCode / $errorDesc")
                viewModel.onSocialLoginFailed(
                    SocialProvider.NAVER,
                    RuntimeException("$errorCode / $errorDesc")
                )
            }
        }

        NidOAuth.requestLogin(this, callback)
    }

}
