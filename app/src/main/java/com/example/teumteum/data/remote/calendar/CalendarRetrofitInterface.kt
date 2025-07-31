package com.example.teumteum.data.remote.calendar

import com.example.teumteum.data.remote.calendar.dto.CalendarResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarRetrofitInterface {
    @GET("/api/home/calendar")
    fun getCalendarData(@Query("startDate") startDate: String, @Query("endDate") endDate: String): Call<CalendarResponse>
}