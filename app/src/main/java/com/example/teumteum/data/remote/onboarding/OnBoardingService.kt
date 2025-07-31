package com.example.teumteum.data.remote.onboarding

import android.util.Log
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobResponse
import com.example.teumteum.data.remote.onboarding.dto.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.dto.ScheduleResponse
import com.example.teumteum.data.remote.wish.WishRetrofitInterface
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.dto.RegisterWishResponse
import com.example.teumteum.ui.signup.view.NicknameJobFieldView
import com.example.teumteum.ui.signup.view.ScheduleView
import com.example.teumteum.utils.getRetrofitWithToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class OnBoardingService {

    private lateinit var nicknameJobFieldView: NicknameJobFieldView

    private lateinit var scheduleView: ScheduleView

    fun setNicknameJobFieldView(nicknameJobFieldView: NicknameJobFieldView) {
        this.nicknameJobFieldView = nicknameJobFieldView
    }

    fun setScheduleView(scheduleView: ScheduleView){
        this.scheduleView = scheduleView
    }

    //온보딩 : 닉네임, 직종 입력
    fun postNicknameAndJobField(request: NicknameJobRequest) {
        val nicknameJobApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = nicknameJobApi.postNicknameAndJobField(request)

        call.enqueue(object : Callback<NicknameJobResponse> {
            override fun onResponse(
                call: Call<NicknameJobResponse>,
                response: Response<NicknameJobResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2002") {
                        nicknameJobFieldView.onNicknameJobSuccess(body.code)
                    } else {
                        nicknameJobFieldView.onNicknameJobFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    nicknameJobFieldView.onNicknameJobFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<NicknameJobResponse>, t: Throwable) {
                nicknameJobFieldView.onNicknameJobFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })

    }

    //반복일정 등록
    fun postSchedules(request: ScheduleRequest) {
        val scheduleApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = scheduleApi.postSchedules(request)

        Log.d("SCHEDULE_REQUEST", request.toString())

        call.enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(
                call: Call<ScheduleResponse>,
                response: Response<ScheduleResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2004") {
                        scheduleView.onScheduleSuccess(body.code)
                    } else {
                        scheduleView.onScheduleFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    scheduleView.onScheduleFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                scheduleView.onScheduleFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })

    }
}