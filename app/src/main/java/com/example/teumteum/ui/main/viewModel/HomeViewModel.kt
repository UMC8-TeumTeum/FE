package com.example.teumteum.ui.main.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.home.repository.HomeRepository
import com.example.teumteum.ui.main.data.TimeBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _scheduleList = MutableLiveData<List<TimeBlock>>(emptyList())
    val scheduleList: LiveData<List<TimeBlock>> = _scheduleList

    private var date: String? = null

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getTodayScheduleIfNeeded() {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        if (date == currentDate) return //이미 호출한 날짜면 패스

        date = currentDate
        getTodaySchedule(currentDate)
    }

    private fun getTodaySchedule(date: String) {
        viewModelScope.launch {
            repository.getTodaySchedule(date)
                .onSuccess { result ->
                    Log.d("TodaySchedule", result.toString())
                    _scheduleList.value = result.map {
                        val start = timeToMinutes(it.startTime)
                        val end = timeToMinutes(it.endTime)
                        TimeBlock(start, end, it.type)
                    }
                }
                .onFailure {
                    _error.value = "스케줄 조회 실패: ${it.message}"
                    Log.d("TodaySchedule", _error.value.toString() )
                }
        }
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        return hour * 60 + minute
    }

    //스케줄이 변경되었을 때 업데이트
    fun refreshTodaySchedule() {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        date = currentDate
        getTodaySchedule(currentDate)
    }
}

