package com.example.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.mypage.model.MyRoutineRequest
import com.example.teumteum.data.remote.mypage.model.MyRoutineResponse
import com.example.teumteum.data.remote.mypage.repository.MyPageRepository
import com.example.teumteum.data.remote.onboarding.model.Week
import com.example.teumteum.ui.myhome.data.MyRoutine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MyRoutineViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    companion object {
        private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    val routineMap: MutableMap<Int, MutableList<MyRoutine>> =
        mutableMapOf<Int, MutableList<MyRoutine>>().apply {
            for (i in 0..6) put(i, mutableListOf())
        }

    private val _currentDayRoutineList = MutableLiveData<List<MyRoutine>>(emptyList())
    val currentDayRoutineList: LiveData<List<MyRoutine>> = _currentDayRoutineList

    private var selectedDayIndex: Int = 0

    fun updateCurrentDaySchedule(dayIndex: Int) {
        selectedDayIndex = dayIndex
        _currentDayRoutineList.value = routineMap[dayIndex]?.toList().orEmpty()
    }

    fun fetchMyRoutine(weekday: Week) {
        val dayIndex = weekToIndex(weekday)

        viewModelScope.launch {
            repository.getMyRoutine(weekday)
                .onSuccess { remoteList ->
                    val uiList = remoteList.map { it.toMyRoutine() }

                    routineMap[dayIndex] = uiList.toMutableList()

                    if (dayIndex == selectedDayIndex) {
                        _currentDayRoutineList.value = uiList
                    }
                }
                .onFailure { e ->
                    Log.e("Routine", "fetchMyRoutine failed: $weekday", e)

                    if (dayIndex == selectedDayIndex) {
                        _currentDayRoutineList.value = emptyList()
                    }
                }
        }
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

    fun addRoutine(
        dayIndex: Int,
        title: String,
        description: String,
        start: LocalTime,
        end: LocalTime
    ) {
        val weekday = indexToWeek(dayIndex)

        val req = MyRoutineRequest(
            title = title,
            description = description,
            weekday = weekday,
            startTime = start.format(TIME_FMT),
            endTime = end.format(TIME_FMT)
        )

        viewModelScope.launch {
            repository.addMyRoutine(req)
                .onSuccess { created ->
                    fetchMyRoutine(weekday)
                }
                .onFailure { e ->
                    Log.e("Routine", "addRoutine failed", e)
                }
        }
    }

    fun modifyRoutine(
        routineId: Long,
        dayIndex: Int,
        title: String,
        description: String,
        start: LocalTime,
        end: LocalTime
    ) {
        val weekday = indexToWeek(dayIndex)

        val req = MyRoutineRequest(
            title = title,
            description = description,
            weekday = weekday,
            startTime = start.format(TIME_FMT),
            endTime = end.format(TIME_FMT)
        )

        viewModelScope.launch {
            repository.modifyMyRoutine(routineId, req)
                .onSuccess { created ->
                    fetchMyRoutine(weekday)
                }
                .onFailure { e ->
                    Log.e("Routine", "addRoutine failed", e)
                }
        }
    }

    fun deleteRoutine(
        routineId: Long,
        dayIndex: Int
    ) {
        val weekday = indexToWeek(dayIndex)

        viewModelScope.launch {
            repository.deleteMyRoutine(routineId)
                .onSuccess { created ->
                    fetchMyRoutine(weekday)
                }
                .onFailure { e ->
                    Log.e("Routine", "addRoutine failed", e)
                }
        }
    }

    private fun indexToWeek(dayIndex: Int): Week = when (dayIndex) {
        0 -> Week.SUNDAY
        1 -> Week.MONDAY
        2 -> Week.TUESDAY
        3 -> Week.WEDNESDAY
        4 -> Week.THURSDAY
        5 -> Week.FRIDAY
        6 -> Week.SATURDAY
        else -> Week.SUNDAY
    }

    private fun MyRoutineResponse.toMyRoutine(): MyRoutine {
        return MyRoutine(
            routineId = routineId,
            title = title,
            weekday = weekday.toString(),
            startTime = LocalTime.parse(startTime, TIME_FMT),
            endTime = LocalTime.parse(endTime, TIME_FMT),
            description = description
        )
    }
}

