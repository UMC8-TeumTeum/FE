package com.example.teumteum.data.remote.calendar.repository

import android.util.Log
import com.example.teumteum.data.remote.calendar.model.GetCalendarResponse
import com.example.teumteum.data.remote.calendar.service.CalendarService
import com.example.teumteum.utils.handleApiResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarService: CalendarService
){
    suspend fun getCalendar(startDate: String, endDate: String): Result<List<GetCalendarResponse>> = runCatching {
        val response = calendarService.getCalendar(startDate, endDate)
        Log.d("HomeCalendar", "response = ${response.body()}")
        handleApiResponse(response)
    }
}