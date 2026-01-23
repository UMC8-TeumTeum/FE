package com.umc.teumteum.data.remote.home.service

import com.umc.teumteum.data.remote.home.model.GetCalendarResponse
import com.umc.teumteum.data.remote.home.model.ScheduleResult
import com.umc.teumteum.data.remote.home.model.TeumTimeResponse
import com.umc.teumteum.data.remote.home.model.TimetableResponse
import com.umc.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    @GET("/api/home/teum")
    suspend fun getScheduleForDate( @Query("date") date: String ): Response<ApiResponse<List<ScheduleResult>>>

    @GET("/api/home/teum-time")
    suspend fun getTeumTime(): Response<ApiResponse<TeumTimeResponse>>

    @GET("/api/home/calendar")
    suspend fun getCalendar(@Query("startDate") startDate: String, @Query("endDate") endDate: String): Response<ApiResponse<List<GetCalendarResponse>>>

    @GET("/api/home/timetable")
    suspend fun getTimetable(@Query("date") date: String ): Response<ApiResponse<TimetableResponse>>
}