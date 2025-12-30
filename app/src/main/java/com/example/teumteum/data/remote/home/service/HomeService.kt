package com.example.teumteum.data.remote.home.service

import com.example.teumteum.data.remote.home.model.GetCalendarResponse
import com.example.teumteum.data.remote.home.model.ScheduleResult
import com.example.teumteum.data.remote.home.model.TeumTimeResponse
import com.example.teumteum.data.remote.home.model.TimetableResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    @GET("/api/home/teum")
    suspend fun getTodaySchedule( @Query("date") date: String ): Response<ApiResponse<List<ScheduleResult>>>

    @GET("/api/home/teum-time")
    suspend fun getTeumTime(): Response<ApiResponse<TeumTimeResponse>>

    @GET("/api/home/calendar")
    suspend fun getCalendar(@Query("startDate") startDate: String, @Query("endDate") endDate: String): Response<ApiResponse<List<GetCalendarResponse>>>

    @GET("/api/home/timetable")
    suspend fun getTimetable(@Query("date") date: String ): Response<ApiResponse<TimetableResponse>>
}