package com.example.teumteum.ui.signup.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.onboarding.model.AgreementRequest
import com.example.teumteum.data.remote.onboarding.model.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.repository.OnBoardingRepository
import com.example.teumteum.utils.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val repository: OnBoardingRepository
) : ViewModel() {

    private val _state = MutableLiveData<OnBoardingUiState>(OnBoardingUiState.Idle)
    val state: LiveData<OnBoardingUiState> get() = _state

    private val _step = MutableLiveData<OnBoardingStep>(OnBoardingStep.Agreement)
    val step: LiveData<OnBoardingStep> get() = _step

    /** 약관 동의  */
    fun postAgreements(request: AgreementRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postAgreements(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                    _step.value = OnBoardingStep.AgreementComplete
                }
                .onFailure { handleError(it) }
        }
    }

    /** 닉네임/직업 등록 */
    fun postNicknameAndJob(request: NicknameJobRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postNicknameAndJobField(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                    _step.value = OnBoardingStep.ProfileImage
                }
                .onFailure { handleError(it) }
        }
    }

    /** 프리사인드 url 요청 */
    fun requestPresignedUrl(request: PresignedRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.requestPresignedUrl(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.PresignedSuccess(
                        presignedUrl = it.presignedUrl,
                        fileName = it.fileName,
                        contentType = request.contentType
                    )
                }
                .onFailure { handleError(it) }
        }
    }

    /** 프로필 이미지 등록 */
    fun postProfileImage(request: ProfileImageRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postProfileImage(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                    _step.value = OnBoardingStep.SleepPattern
                }
                .onFailure { handleError(it) }
        }
    }

    /** 수면 패턴 등록 (선택적) */
    fun postSleepPattern(request: SleepPatternRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postSleepPattern(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                    _step.value = OnBoardingStep.Schedule
                }
                .onFailure { handleError(it) }
        }
    }

    /** 반복 일정 등록 (선택적) */
    fun postSchedule(request: ScheduleRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postSchedules(request)
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                    _step.value = OnBoardingStep.Reminder
                }
                .onFailure { handleError(it) }
        }
    }

    /** 리마인드 알림 등록 (선택적) */
//    fun postReminder(request: ReminderRequest) {
//        _state.value = OnBoardingUiState.Loading
//        viewModelScope.launch {
//            repository.postReminder(request)
//                .onSuccess {
//                    _state.value = OnBoardingUiState.Success
//                    _step.value = OnBoardingStep.Complete
//                }
//                .onFailure { handleError(it) }
//        }
//    }

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

