package com.example.teumteum.data.remote.calendar.service

import com.example.teumteum.data.remote.calendar.model.GetCalendarResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarService {
    @GET("/api/home/calendar")
    suspend fun getCalendar(@Query("startDate") startDate: String, @Query("endDate") endDate: String): Response<ApiResponse<List<GetCalendarResponse>>>
}