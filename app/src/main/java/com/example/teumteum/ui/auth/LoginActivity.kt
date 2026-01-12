package com.example.teumteum.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Base64
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
import java.security.SecureRandom
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
        val nonce = generateNonce()
        Log.d("KAKAO_NONCE", "generated nonce=$nonce")

        val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = callback@{ token, error ->
            if (error != null) {
                viewModel.onSocialLoginFailed(SocialProvider.KAKAO, error)
                return@callback
            }

            if (token == null) return@callback

            val idToken = token.idToken
            if (idToken.isNullOrBlank()) {
                Log.e("KAKAO", "idToken is null/blank. Check Kakao OIDC(OpenID Connect) settings.")
                viewModel.onSocialLoginFailed(
                    SocialProvider.KAKAO,
                    RuntimeException("Kakao id_token이 발급되지 않았습니다.")
                )
                return@callback
            }

            Log.d("KAKAO_SEND", "send nonce=$nonce, idToken.len=${idToken.length}")

            viewModel.exchangeKakaoToken(idToken, nonce)
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, nonce = nonce) { token, error ->
                if (error != null) {
                    UserApiClient.instance.loginWithKakaoAccount(this, nonce = nonce, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, nonce = nonce, callback = callback)
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

            val nonce = generateNonce()
            Log.d("GOOGLE_NONCE", "generated nonce=$nonce")

            val idToken = requestGoogleIdToken(credentialManager, nonce) ?: return@launch

            Log.d("GOOGLE_SEND", "send nonce=$nonce, idToken.len=${idToken.length}")

            viewModel.exchangeGoogleToken(idToken, nonce)
        }
    }


    private suspend fun requestGoogleIdToken(cm: CredentialManager, nonce: String): String? {
        val webClientId = BuildConfig.GOOGLE_CLIENT_ID

        val first = buildGoogleRequest(webClientId, filterByAuthorizedAccounts = true, nonce = nonce)
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

        val second = buildGoogleRequest(webClientId, filterByAuthorizedAccounts = false, nonce = nonce)
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
        filterByAuthorizedAccounts: Boolean,
        nonce: String
    ): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
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

    private fun generateNonce(byteSize: Int = 32): String {
        val bytes = ByteArray(byteSize)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
