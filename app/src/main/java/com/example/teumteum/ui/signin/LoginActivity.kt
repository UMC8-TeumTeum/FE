package com.example.teumteum.ui.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.teumteum.ui.signup.SignUpActivity
import com.example.teumteum.databinding.ActivityLoginBinding
import com.example.teumteum.ui.signin.data.LoginResult
import com.example.teumteum.ui.signin.viewModel.LoginViewModel
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupLayout.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.kakaoLoginBtn.setOnClickListener {
            startKakaoLogin()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Loading -> {
                    // TODO: 로딩 처리
                }
                is LoginResult.Success -> {
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                }
                is LoginResult.Error -> {
                    Log.d("KakaoLogin", "카카오 로그인 실패 ${result.message}")
//                    Toast.makeText(this, result.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startKakaoLogin() {
        val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                viewModel.onKakaoLoginFailed(error)
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

}
