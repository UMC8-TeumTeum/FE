package com.example.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.mypage.model.PushAlarmRequest
import com.example.teumteum.data.remote.mypage.model.RemindAlarmRequest
import com.example.teumteum.data.remote.mypage.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: SettingRepository
) : ViewModel() {

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _remindAlarms = MutableLiveData<List<Int>>()
    val remindAlarms: LiveData<List<Int>> = _remindAlarms

    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving

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
}