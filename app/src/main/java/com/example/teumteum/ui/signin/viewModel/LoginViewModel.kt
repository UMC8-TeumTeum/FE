package com.example.teumteum.ui.signin.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.alarm.FcmRepository
import com.example.teumteum.data.remote.login.repository.LoginRepository
import com.example.teumteum.ui.signin.data.LoginResult
import com.example.teumteum.utils.FcmTokenStore
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.TokenProvider
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val tokenProvider: TokenProvider,
    private val flowPrefs: FlowPrefs,
    private val fcmRepository: FcmRepository,
    private val fcmTokenStore: FcmTokenStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun exchangeKakaoToken(kakaoAccessToken: String) {
        _loginResult.value = LoginResult.Loading
        getJwtFromServer(kakaoAccessToken)
    }

    fun onKakaoLoginFailed(t: Throwable) {
        _loginResult.postValue(LoginResult.Error("카카오 로그인 실패: ${t.message}"))
    }

    private fun getJwtFromServer(kakaoAccessToken: String) {
        viewModelScope.launch {
            repository.loginWithKakaoAccessToken(kakaoAccessToken)
                .onSuccess { res ->
                    // 1) 액세스/리프레시 토큰 저장
                    tokenProvider.saveTokens(res.accessToken, res.refreshToken)
                    // 2) 다음 스텝 저장
                    flowPrefs.setLastStep(res.nextStep)
                    // 3) FCM 토큰 동기화
                    syncFcmTokenAfterLogin()
                    // 4) 로그인 성공 알림 (FCM 업로드는 백그라운드로 진행)
                    _loginResult.value = LoginResult.Success(res.nextStep)
                }
                .onFailure { e ->
                    _loginResult.value = LoginResult.Error("서버 로그인 실패: ${e.message}")
                }
        }
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