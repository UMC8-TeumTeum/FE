package com.example.teumteum.ui.onboarding.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.onboarding.model.AgreementRequest
import com.example.teumteum.ui.onboarding.data.Schedule
import com.example.teumteum.data.remote.onboarding.model.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.data.remote.onboarding.model.RemindRequest
import com.example.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.repository.OnBoardingRepository
import com.example.teumteum.utils.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val repository: OnBoardingRepository
) : ViewModel() {

    private val _state = MutableLiveData<OnBoardingUiState>(OnBoardingUiState.Idle)
    val state: LiveData<OnBoardingUiState> get() = _state

    val scheduleMap = mutableMapOf<Int, MutableList<Schedule>>()

    private val _scheduleAdded = MutableLiveData<Unit>()
    val scheduleAdded: LiveData<Unit> get() = _scheduleAdded

    private val _currentDayScheduleList = MutableLiveData<List<Schedule>>()
    val currentDayScheduleList: LiveData<List<Schedule>> get() = _currentDayScheduleList

    private val _sleepStartTime = MutableLiveData<LocalTime?>()
    val sleepStartTime: LiveData<LocalTime?> = _sleepStartTime

    private val _sleepEndTime = MutableLiveData<LocalTime?>()
    val sleepEndTime: LiveData<LocalTime?> = _sleepEndTime

    private val _nickname = MutableLiveData<String?>()
    val nickname: LiveData<String?> = _nickname

    private val _field = MutableLiveData<String?>()
    val field: LiveData<String?> = _field

    private val _profileImageUri = MutableLiveData<Uri?>()
    val profileImageUri: LiveData<Uri?> = _profileImageUri

    private val _profileImageFileName = MutableLiveData<String?>()

    private val _remindList = MutableLiveData<List<Int>>(emptyList())
    val remindList: LiveData<List<Int>> get() = _remindList

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

    /** 닉네임/직업 등록 */
    fun postNicknameAndJob() {
        val nickname = _nickname.value.orEmpty()
        val jobField = _field.value.orEmpty()

        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postNicknameAndJobField(NicknameJobRequest(nickname, jobField))
                .onSuccess {
                    _state.value = OnBoardingUiState.Success
                }
                .onFailure { handleError(it) }
        }
    }

    /** 프리사인드 url 요청 후 실제 S3에 프로필 이미지 등록 */
    fun uploadProfileImage(context: Context) {
        val uri = _profileImageUri.value ?: run {
            _state.value = OnBoardingUiState.Success
            return
        }

        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
        _state.value = OnBoardingUiState.Loading

        viewModelScope.launch {
            repository.requestPresignedUrl(PresignedRequest(contentType))
                .onSuccess { response ->
                    _profileImageFileName.value = response.fileName

                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: run {
                        _state.value = OnBoardingUiState.Error("UPLOAD_FAIL", "이미지를 불러올 수 없습니다.")
                        return@onSuccess
                    }

                    val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())
                    val request = Request.Builder().url(response.presignedUrl).put(requestBody).build()

                    OkHttpClient().newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            _state.postValue(
                                OnBoardingUiState.Error(
                                    "UPLOAD_FAIL",
                                    "이미지 업로드 실패: ${e.message}"
                                )
                            )
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            if (response.isSuccessful) {
                                postProfileImage(
                                    ProfileImageRequest(
                                        _profileImageFileName.value!!
                                    )
                                )
                            } else {
                                _state.postValue(
                                    OnBoardingUiState.Error(
                                        "UPLOAD_FAIL",
                                        "이미지 업로드 실패 (code: ${response.code})"
                                    )
                                )
                            }
                        }
                    })
                }
                .onFailure {
                    handleError(it)
                }
        }
    }

    fun postProfileImage(request: ProfileImageRequest) {
        _state.postValue(OnBoardingUiState.Loading)
        viewModelScope.launch {
            repository.postProfileImage(request)
                .onSuccess {
                    _state.postValue(OnBoardingUiState.Success)
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
                }
                .onFailure { handleError(it) }
        }
    }

    //반복 일정 추가
    fun addSchedule(dayIndex: Int, schedule: Schedule) {
        val list = scheduleMap.getOrPut(dayIndex) { mutableListOf() }
        list.add(schedule)
        _scheduleAdded.value = Unit

        if (_currentDayScheduleList.value != null && dayIndexMatchesCurrentList(dayIndex)) {
            _currentDayScheduleList.value = list.toList()
        }
    }

    private fun dayIndexMatchesCurrentList(dayIndex: Int): Boolean {
        val current = _currentDayScheduleList.value ?: return false
        val target = scheduleMap[dayIndex]?.toList() ?: return false
        return current.size != target.size || current != target
    }


    /** 리마인드 알림 등록 (선택적) */
    fun postRemind(request: RemindRequest) {
        _state.value = OnBoardingUiState.Loading
        viewModelScope.launch {
            repository.postRemind(request)
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

    //수면 시간 설정
    fun setSleepStartTime(start: LocalTime) {
        _sleepStartTime.value = start
    }

    fun setSleepEndTime(end: LocalTime) {
        _sleepEndTime.value = end
    }

    // 닉네임, 직종 저장
    fun setNickname(value: String) {
        _nickname.value = value
    }

    fun setField(value: String) {
        _field.value = value
    }

    fun setProfileImage(uri: Uri) {
        _profileImageUri.value = uri
    }

    fun updateCurrentDaySchedule(dayIndex: Int) {
        val list = scheduleMap[dayIndex]?.toList() ?: emptyList()
        _currentDayScheduleList.value = list
    }

    fun toggleReminder(minute: Int, enabled: Boolean) {
        val current = _remindList.value?.toMutableList() ?: mutableListOf()
        if (enabled) {
            if (!current.contains(minute)) current.add(minute)
        } else {
            current.remove(minute)
        }
        _remindList.value = current.sorted()
    }
}

