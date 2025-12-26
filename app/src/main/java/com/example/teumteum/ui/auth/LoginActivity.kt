package com.example.teumteum.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.teumteum.BuildConfig
import com.example.teumteum.databinding.ActivityLoginBinding
import com.example.teumteum.ui.auth.data.LoginResult
import com.example.teumteum.ui.auth.data.SocialProvider
import com.example.teumteum.ui.auth.viewModel.LoginViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.NextStep
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

        binding.googleLoginBtn.setOnClickListener {
            startGoogleLogin()
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

    private fun startGoogleLogin() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@LoginActivity)

            val idToken = requestGoogleIdToken(credentialManager) ?: return@launch

            viewModel.exchangeSocialToken(SocialProvider.GOOGLE, idToken)
        }
    }

    private suspend fun requestGoogleIdToken(cm: CredentialManager): String? {
        val webClientId = BuildConfig.GOOGLE_CLIENT_ID

        val first = buildGoogleRequest(webClientId, filterByAuthorizedAccounts = true)
        try {
            val result = cm.getCredential(
                context = this@LoginActivity,
                request = first
            )
            return extractGoogleIdToken(result)
        } catch (e: NoCredentialException) {
            Log.d("GOOGLE", "No authorized credential. Retry with all accounts.")
        } catch (e: GetCredentialCancellationException) {
            Log.d("GOOGLE", "User canceled Google sign-in.")
            viewModel.onSocialLoginFailed(SocialProvider.GOOGLE, RuntimeException("Google sign-in canceled"))
            return null
        } catch (e: GetCredentialException) {
            Log.e("GOOGLE", "GetCredentialException: ${e.message}", e)
            viewModel.onSocialLoginFailed(SocialProvider.GOOGLE, e)
            return null
        }

        val second = buildGoogleRequest(webClientId, filterByAuthorizedAccounts = false)
        try {
            val result = cm.getCredential(
                context = this@LoginActivity,
                request = second
            )
            return extractGoogleIdToken(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d("GOOGLE", "User canceled Google sign-in.")
            viewModel.onSocialLoginFailed(SocialProvider.GOOGLE, RuntimeException("Google sign-in canceled"))
            return null
        } catch (e: GetCredentialException) {
            Log.e("GOOGLE", "GetCredentialException: ${e.message}", e)
            viewModel.onSocialLoginFailed(SocialProvider.GOOGLE, e)
            return null
        }
    }

    private fun buildGoogleRequest(
        webClientId: String,
        filterByAuthorizedAccounts: Boolean
    ): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    private fun extractGoogleIdToken(response: GetCredentialResponse): String? {
        val credential: Credential = response.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            return googleCred.idToken
        }

        Log.w("GOOGLE", "Credential is not Google ID token type.")
        return null
    }
}
