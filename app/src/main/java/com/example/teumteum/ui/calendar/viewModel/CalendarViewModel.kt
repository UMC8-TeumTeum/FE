package com.example.teumteum.ui.calendar.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.calendar.model.GetCalendarResponse
import com.example.teumteum.data.remote.calendar.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _calendarData = MutableLiveData<GetCalendarResponse>()
    val calendarData: LiveData<GetCalendarResponse> = _calendarData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 캘린더 일정 조회
    private fun getCalendar(startDate: String, endDate: String) {
        viewModelScope.launch {
            val result = repository.getCalendar(startDate, endDate)

            result.onSuccess {
                _calendarData.value = it
            }.onFailure { e ->
                _error.value = e.localizedMessage ?: "캘린더 조회에 실패했습니다."
            }
        }
    }
}