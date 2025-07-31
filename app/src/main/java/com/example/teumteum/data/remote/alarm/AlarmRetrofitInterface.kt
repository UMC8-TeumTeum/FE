package com.example.teumteum.data.remote.alarm

import com.example.teumteum.data.remote.alarm.dto.AlarmResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AlarmRetrofitInterface {

    @GET("api/notifications")
    fun getAlarmList(@Query("duration") duration: String, @Query("page") page: Int): Call<AlarmResponse>
}