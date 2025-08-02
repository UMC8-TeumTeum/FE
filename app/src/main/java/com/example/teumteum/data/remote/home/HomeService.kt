package com.example.teumteum.data.remote.home

import com.example.teumteum.data.remote.home.dto.ScheduleResponse
import com.example.teumteum.ui.main.view.HomeView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class HomeService @Inject constructor(
    private val homeApi: HomeRetrofitInterface
){
    private lateinit var homeView: HomeView

    fun setHomeView(homeView: HomeView){
        this.homeView = homeView
    }

    fun getTodaySchedule(date: String) {

        homeApi.getTodaySchedule(date).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(
                call: Call<ScheduleResponse>,
                response: Response<ScheduleResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "HOME20010") {
                        homeView.onScheduleSuccess(body.code, body.result)
                    } else {
                        homeView.onScheduleFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    homeView.onScheduleFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                homeView.onScheduleFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })
    }
}