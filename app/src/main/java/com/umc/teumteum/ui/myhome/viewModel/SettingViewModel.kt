package com.umc.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.teumteum.data.remote.mypage.model.AlarmSettingResponse
import com.umc.teumteum.data.remote.mypage.model.PushAlarmRequest
import com.umc.teumteum.data.remote.mypage.model.RemindAlarmRequest
import com.umc.teumteum.data.remote.mypage.repository.SettingRepository
import com.umc.teumteum.data.remote.onboarding.model.SleepPatternRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data object Success : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: SettingRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _remindAlarms = MutableLiveData<List<Int>>()
    val remindAlarms: LiveData<List<Int>> = _remindAlarms

    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving

    fun resetState() {
        _state.value = UiState.Idle
    }
    private val _alarmSetting = MutableLiveData<AlarmSettingResponse>()
    val alarmSetting: LiveData<AlarmSettingResponse> = _alarmSetting

    fun getRemindAlarms() {
        viewModelScope.launch {
            repository.getRemindAlarms()
                .onSuccess { result ->
                    _remindAlarms.value = result.remindAlarms
                }
                .onFailure {
                    _error.value = "내 정보 조회 실패: ${it.message}"
                    Log.d("Setting", _error.value.toString() )
                }
        }
    }

    fun getAlarmSettings() {
        viewModelScope.launch {
            repository.getAlarmSettings()
                .onSuccess { result ->
                    _alarmSetting.value = result
                }
                .onFailure {
                    _error.value = "알림 설정 조회 실패: ${it.message}"
                    Log.d("Setting", _error.value.toString())
                }
        }
    }


    fun updateRemindAlarms(minutes: List<Int>) {
        viewModelScope.launch {
            _saving.value = true

            val request = RemindAlarmRequest(remindAlarms = minutes)

            repository.updateRemindAlarms(request)
                .onSuccess {
                    _remindAlarms.value = minutes
                }
                .onFailure {
                    _error.value = "알림 설정 저장 실패: ${it.message}"
                }

            _saving.value = false
        }
    }

    fun updatePushAlarmSetting(request: PushAlarmRequest) {
        viewModelScope.launch {
            repository.updatePushAlarms(request)
                .onFailure {
                    _error.value = "알림 설정 저장 실패: ${it.message}"
                    Log.d("Setting", _error.value.toString())
                }
        }
    }

    fun updateSleepPattern(request: SleepPatternRequest) {
        viewModelScope.launch {
            _saving.value = true
            _state.value = UiState.Loading

            repository.updateSleepPattern(request)
                .onSuccess {
                    _state.value = UiState.Success
                }
                .onFailure {
                    val msg = "수면패턴 설정 저장 실패: ${it.message}"
                    _error.value = msg
                    _state.value = UiState.Error(msg)
                    Log.d("Setting", msg)
                }

            _saving.value = false
        }
    }

    fun deleteSleepPattern() {
        viewModelScope.launch {
            repository.deleteSleepPattern()
                .onSuccess {
                    _state.value = UiState.Success
                }
                .onFailure {
                    val msg = "수면패턴 설정 저장 실패: ${it.message}"
                    _error.value = msg
                    _state.value = UiState.Error(msg)
                    Log.d("Setting", msg)
                }
        }
    }
}