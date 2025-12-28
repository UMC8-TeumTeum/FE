package com.example.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.mypage.repository.MyPageRepository
import com.example.teumteum.data.remote.onboarding.model.Week
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.example.teumteum.data.remote.onboarding.model.Schedule as RemoteSchedule
import com.example.teumteum.ui.onboarding.data.Schedule as UiSchedule

@HiltViewModel
class MyRoutineViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    val scheduleMap: MutableMap<Int, MutableList<UiSchedule>> =
        mutableMapOf<Int, MutableList<UiSchedule>>().apply {
            for (i in 0..6) put(i, mutableListOf())
        }

    private val _currentDayScheduleList = MutableLiveData<List<UiSchedule>>(emptyList())
    val currentDayScheduleList: LiveData<List<UiSchedule>> = _currentDayScheduleList

    private var selectedDayIndex: Int = 0

    fun updateCurrentDaySchedule(dayIndex: Int) {
        selectedDayIndex = dayIndex
        _currentDayScheduleList.value = scheduleMap[dayIndex]?.toList().orEmpty()
    }

    fun fetchMyRoutine(weekday: Week) {
        val dayIndex = weekToIndex(weekday)

        viewModelScope.launch {
            repository.getMyRoutine(weekday)
                .onSuccess { remoteList ->
                    val uiList = remoteList.map { it.toUiSchedule() }

                    scheduleMap[dayIndex] = uiList.toMutableList()

                    if (dayIndex == selectedDayIndex) {
                        _currentDayScheduleList.value = uiList
                    }
                }
                .onFailure { e ->
                    Log.e("Routine", "fetchMyRoutine failed: $weekday", e)

                    if (dayIndex == selectedDayIndex) {
                        _currentDayScheduleList.value = emptyList()
                    }
                }
        }
    }

    private fun RemoteSchedule.toUiSchedule(): UiSchedule {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        return UiSchedule(
            title = title,
            day = weekday.toString(),
            startTime = LocalTime.parse(startTime, fmt),
            endTime = LocalTime.parse(endTime, fmt),
            description = description
        )
    }

    private fun weekToIndex(week: Week): Int = when (week) {
        Week.SUNDAY -> 0
        Week.MONDAY -> 1
        Week.TUESDAY -> 2
        Week.WEDNESDAY -> 3
        Week.THURSDAY -> 4
        Week.FRIDAY -> 5
        Week.SATURDAY -> 6
    }
}
