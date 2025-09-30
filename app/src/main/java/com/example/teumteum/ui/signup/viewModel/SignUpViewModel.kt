package com.example.teumteum.ui.signup.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.signup.model.AgreementRequest
import com.example.teumteum.data.remote.signup.repository.SignUpRepository
import com.example.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.example.teumteum.utils.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: SignUpRepository
) : ViewModel() {

    private val _state = MutableLiveData<OnBoardingUiState>(OnBoardingUiState.Idle)
    val state: LiveData<OnBoardingUiState> get() = _state

    /** 약관 동의  */
    fun postAgreements(request: AgreementRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postAgreements(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                }
                .onFailure { handleError(it) }
        }
    }

    /** 상태 초기화 */
    fun resetState() {
        _state.value = OnBoardingUiState.Idle
    }

    /** 공통 에러 처리 */
    private fun handleError(e: Throwable) {
        if (e is ApiException) {
            _state.value = OnBoardingUiState.Error(
                code = e.code,
                message = e.message
            )
        } else {
            _state.value = OnBoardingUiState.Error(
                code = "NETWORK_ERROR",
                message = "네트워크 오류가 발생했습니다."
            )
        }
    }
}