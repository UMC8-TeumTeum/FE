package com.example.teumteum.data.remote.home

import com.example.teumteum.data.remote.home.dto.ScheduleResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeRetrofitInterface {
    @GET("/api/home/teum")
    fun getTodaySchedule( @Query("date") date: String ): Call<ScheduleResponse>

}