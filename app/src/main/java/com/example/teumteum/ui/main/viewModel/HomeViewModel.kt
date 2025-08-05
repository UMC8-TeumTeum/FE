package com.example.teumteum.ui.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.home.repository.HomeRepository
import com.example.teumteum.ui.main.data.TimeBlock
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.utils.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _scheduleList = MutableLiveData<List<TimeBlock>>(emptyList())
    val scheduleList: LiveData<List<TimeBlock>> = _scheduleList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getTodaySchedule(date: String) {
        viewModelScope.launch {
            repository.getTodaySchedule(date)
                .onSuccess { result ->
                    _scheduleList.value = result.map {
                        val start = timeToMinutes(it.startTime)
                        val end = timeToMinutes(it.endTime)
                        TimeBlock(start, end, it.type)
                    }
                }
                .onFailure {
                    _error.value = "스케줄 조회 실패: ${it.message}"
                }
        }
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        return hour * 60 + minute
    }

}

