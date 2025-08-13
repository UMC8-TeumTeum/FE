package com.example.teumteum.ui.signin.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.login.repository.LoginRepository
import com.example.teumteum.ui.signin.data.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
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
            Log.d("requestKakaoToken", kakaoAccessToken)
            repository.loginWithKakaoAccessToken(kakaoAccessToken)
                .onSuccess { jwt ->
                    Log.d("JwtToken", "AccessToken ${jwt.accessToken}, RefreshToken ${jwt.refreshToken}")
                    saveTokens(jwt.accessToken, jwt.refreshToken)
                    _loginResult.value = LoginResult.Success(jwt.accessToken)
                }
                .onFailure { e ->
                    _loginResult.value = LoginResult.Error("서버 로그인 실패: ${e.message}")
                }
        }
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("accessToken", accessToken)
            putString("refreshToken", refreshToken)
            apply()
        }
    }
}