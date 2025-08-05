package com.example.teumteum.data.remote.home.service

import com.example.teumteum.data.remote.home.model.ScheduleResult
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    @GET("/api/home/teum")
    fun getTodaySchedule( @Query("date") date: String ): Response<ApiResponse<List<ScheduleResult>>>

}