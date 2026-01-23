package com.umc.teumteum.ui.activity.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.teumteum.data.remote.activity.model.ActivityAiRequest
import com.umc.teumteum.data.remote.activity.model.ActivityAiResult
import com.umc.teumteum.data.remote.activity.model.ActivityWishRequest
import com.umc.teumteum.data.remote.activity.model.ActivityWishResult
import com.umc.teumteum.data.remote.activity.model.AssignAiRequest
import com.umc.teumteum.data.remote.activity.model.AssignWishRequest
import com.umc.teumteum.data.remote.activity.repository.ActivityRepository
import com.umc.teumteum.ui.activity.data.FillingFormState
import com.umc.teumteum.utils.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _errorState = MutableLiveData<ApiException>()
    val errorState: LiveData<ApiException> = _errorState

    private val _errorCode = MutableLiveData<String?>()
    val errorCode: LiveData<String?> = _errorCode

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _activityWishSuccess = MutableLiveData<Boolean>()
    val activityWishSuccess: LiveData<Boolean> get() = _activityWishSuccess

    private val _activityWishes = MutableLiveData<List<ActivityWishResult>>()
    val activityWishes: LiveData<List<ActivityWishResult>> get() = _activityWishes

    private val _activityAiSuccess = MutableLiveData<Boolean>()
    val activityAiSuccess: LiveData<Boolean> get() = _activityAiSuccess

    private val _activityAiContents = MutableLiveData<List<ActivityAiResult>>()
    val activityAiContents: LiveData<List<ActivityAiResult>> get() = _activityAiContents

    private val _assignSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val assignSuccess: SharedFlow<Unit> = _assignSuccess.asSharedFlow()

    private val _assignError = MutableSharedFlow<ApiException>(replay = 0, extraBufferCapacity = 1)
    val assignError: SharedFlow<ApiException> = _assignError.asSharedFlow()

    private val _fillingFormState = MutableLiveData(FillingFormState())
    val fillingFormState: LiveData<FillingFormState> = _fillingFormState

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private var pendingCount = 0
    private fun startLoading() {
        if (pendingCount++ == 0) _loading.postValue(true)
    }
    private fun endLoading() {
        pendingCount = (pendingCount - 1).coerceAtLeast(0)
        if (pendingCount == 0) _loading.postValue(false)
    }

    // 채움활동 위시리스트 불러오기
    fun activityWish(request: ActivityWishRequest) {
        viewModelScope.launch {
            startLoading()
            try {
                val result = activityRepository.activityWish(request)
                result.onSuccess { response ->
                    _activityWishSuccess.value = true
                    _activityWishes.value = response.wishes
                        ?.filter { it.title.isNotBlank() }
                        .orEmpty()
                }
                result.onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "채움활동 위시 조회에 실패했습니다."
                }
            } finally {
                endLoading()
            }
        }
    }

    // 채움활동 AI 컨텐츠 불러오기
    fun activityAi(request: ActivityAiRequest) {
        viewModelScope.launch {
            startLoading()
            try {
                val result = activityRepository.activityAi(request)
                result.onSuccess { response ->
                    _activityAiSuccess.value = true
                    _activityAiContents.value = response.aiContents
                        ?.filter { it.title.isNotBlank() }
                        .orEmpty()
                }
                result.onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "채움활동 AI 컨텐츠 조회에 실패했습니다."
                }
            } finally {
                endLoading()
            }
        }
    }

    // 위시 빈틈 채우기
    fun assignWish(wishId: Long, request: AssignWishRequest) {
        viewModelScope.launch {
            val result = activityRepository.assignWish(wishId, request)
            result.onSuccess {
                _assignSuccess.tryEmit(Unit)
            }
            result.onFailure { e ->
                val apiEx = e as? ApiException
                val code = apiEx?.code
                val msg = apiEx?.message ?: e.localizedMessage ?: "위시 빈틈채우기에 실패했습니다."

                _errorCode.value = code
                _errorMessage.value = msg
                _errorState.value = code?.let { ApiException(it, msg) }

                _assignError.tryEmit(ApiException(code ?: "UNKNOWN", msg))
            }
        }
    }

    // ai컨텐츠 빈틈 채우기
    fun assignAi(request: AssignAiRequest) {
        viewModelScope.launch {
            val result = activityRepository.assignAi(request)
            result.onSuccess {
                _assignSuccess.tryEmit(Unit)
            }
            result.onFailure { e ->
                val apiEx = e as? ApiException
                val code = apiEx?.code
                val msg = apiEx?.message ?: e.localizedMessage ?: "ai컨텐츠 빈틈채우기에 실패했습니다."

                _errorCode.value = code
                _errorMessage.value = msg
                _errorState.value = code?.let { ApiException(it, msg) }

                _assignError.tryEmit(ApiException(code ?: "UNKNOWN", msg))
            }
        }
    }

    fun updateFillingFormState(reducer: (FillingFormState) -> FillingFormState) {
        _fillingFormState.value = reducer(_fillingFormState.value ?: FillingFormState())
    }

    fun clearFillingFormState() {
        _fillingFormState.value = FillingFormState()
    }

    fun clearActivityResults() {
        _activityWishes.value = emptyList()
        _activityAiContents.value = emptyList()
        _activityWishSuccess.value = false
        _activityAiSuccess.value = false
    }
}