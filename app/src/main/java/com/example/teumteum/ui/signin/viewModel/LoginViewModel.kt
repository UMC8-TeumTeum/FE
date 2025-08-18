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
                    tokenProvider.saveTokens(res.accessToken, res.refreshToken)
                    flowPrefs.setLastStep(res.nextStep)

                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { token ->
                            viewModelScope.launch {
                                fcmRepository.registerToken(token)
                                    .onFailure { e -> Log.e("FCM", "FCM 등록 실패: ${e.message}") }
                            }
                        }

                    _loginResult.value = LoginResult.Success(res.nextStep)
                }
                .onFailure { e ->
                    _loginResult.value = LoginResult.Error("서버 로그인 실패: ${e.message}")
                }
        }
    }

}