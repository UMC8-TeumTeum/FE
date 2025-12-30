package com.example.teumteum.data.remote.home.repository

import android.util.Log
import com.example.teumteum.data.remote.home.model.GetCalendarResponse
import com.example.teumteum.data.remote.home.model.ScheduleResult
import com.example.teumteum.data.remote.home.model.TeumTimeResponse
import com.example.teumteum.data.remote.home.model.TimetableResponse
import com.example.teumteum.data.remote.home.service.HomeService
import com.example.teumteum.utils.handleApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val homeService: HomeService
){
    suspend fun getTodaySchedule(date: String): Result<List<ScheduleResult>> = runCatching {
        val response = homeService.getTodaySchedule(date)
        Log.d("HomeSchedule", "response = ${response.body()}")
        handleApiResponse(response)
    }

    suspend fun getTeumTime(): Result<TeumTimeResponse> = runCatching {
        val response = homeService.getTeumTime()
        Log.d("HomeSchedule", "response = ${response.body()}")
        handleApiResponse(response)
    }

    suspend fun getCalendar(startDate: String, endDate: String): Result<List<GetCalendarResponse>> = runCatching {
        val response = homeService.getCalendar(startDate, endDate)
        Log.d("HomeCalendar", "response = ${response.body()}")
        handleApiResponse(response)
    }

    suspend fun getTimetable(date: String): Result<TimetableResponse> = runCatching {
        val response = homeService.getTimetable(date)
        Log.d("HomeTimetable", "response = ${response.body()}")
        handleApiResponse(response)
    }
}
