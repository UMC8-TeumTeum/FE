package com.example.teumteum.ui.auth.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.alarm.FcmTokenStore
import com.example.teumteum.data.remote.alarm.repository.FcmRepository
import com.example.teumteum.data.remote.auth.repository.AuthRepository
import com.example.teumteum.ui.auth.data.LoginResult
import com.example.teumteum.ui.auth.data.SocialProvider
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.NextStep
import com.example.teumteum.utils.TokenProvider
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenProvider: TokenProvider,
    private val flowPrefs: FlowPrefs,
    private val fcmRepository: FcmRepository,
    private val fcmTokenStore: FcmTokenStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun exchangeSocialToken(provider: SocialProvider, token: String) {
        if (token.isBlank()) {
            _loginResult.value = LoginResult.Error("${provider.name} 토큰이 비어있습니다.")
            return
        }

        _loginResult.value = LoginResult.Loading

        viewModelScope.launch {
            repository.loginWithSocialAccessToken(provider, token)
                .onSuccess { res ->
                    onLoginSuccess(res.accessToken, res.refreshToken, res.nextStep)
                }
                .onFailure { e ->
                    Log.e("SocialLogin", "서버 로그인 실패 provider=${provider.name}, msg=${e.message}", e)
                    _loginResult.value = LoginResult.Error("서버 로그인 실패: ${e.message}")
                }
        }
    }


    fun onSocialLoginFailed(provider: SocialProvider, t: Throwable) {
        Log.e("SocialLogin", "${provider.name} 로그인 실패: ${t.message}", t)
        _loginResult.postValue(LoginResult.Error("${provider.name} 로그인 실패: ${t.message}"))
    }

    fun exchangeKakaoToken(kakaoAccessToken: String) =
        exchangeSocialToken(SocialProvider.KAKAO, kakaoAccessToken)

    fun exchangeNaverToken(naverAccessToken: String) =
        exchangeSocialToken(SocialProvider.NAVER, naverAccessToken)

    fun exchangeGoogleToken(googleToken: String) =
        exchangeSocialToken(SocialProvider.GOOGLE, googleToken)


    // 로그인 성공 공통 처리
    private fun onLoginSuccess(accessToken: String, refreshToken: String, nextStep: NextStep) {
        //액세스/리프레시 토큰 저장
        tokenProvider.saveTokens(accessToken, refreshToken)

        //다음 스텝 저장
        flowPrefs.setLastStep(nextStep)

        //FCM 토큰 동기화 (백그라운드)
        syncFcmTokenAfterLogin()

        //UI 전환 트리거
        _loginResult.value = LoginResult.Success(nextStep)
    }

    /**
     * 로그인 직후 FCM 토큰을 서버에 반영
     * - onNewToken()에서 캐시해둔 값이 있으면 그걸 우선 사용
     * - 없으면 liveToken 사용
     * - 업로드 성공 시 캐시 최신화
     */
    private fun syncFcmTokenAfterLogin() {
        // 우선 캐시 확인
        val cached = fcmTokenStore.load()

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { liveToken ->
                val tokenToUpload = if (!cached.isNullOrBlank()) cached else liveToken
                uploadFcmToken(tokenToUpload)
            }
            .addOnFailureListener { e ->
                Log.w("FCM", "라이브 토큰 조회 실패: ${e.message}")
                // 라이브 토큰을 못 받아도, 캐시가 있으면 그걸로 업로드 시도
                if (!cached.isNullOrBlank()) {
                    uploadFcmToken(cached)
                }
            }
    }

    /**
     * 서버에 FCM 토큰을 업로드하고, 성공 시 캐시 갱신
     */
    private fun uploadFcmToken(token: String) {
        if (token.isBlank()) return

        viewModelScope.launch {
            fcmRepository.registerToken(token)
                .onSuccess {
                    // 서버 반영 성공 → 캐시 최신화
                    fcmTokenStore.save(token)
                    Log.d("FCM", "FCM 등록 성공")
                }
                .onFailure { e ->
                    Log.e("FCM", "FCM 등록 실패: ${e.message}")
                }
        }
    }

}