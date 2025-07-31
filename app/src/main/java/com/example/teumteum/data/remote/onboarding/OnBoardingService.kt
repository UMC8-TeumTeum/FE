package com.example.teumteum.data.remote.onboarding

import android.util.Log
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobResponse
import com.example.teumteum.data.remote.onboarding.dto.SleepPatternRequest
import com.example.teumteum.data.remote.onboarding.dto.SleepPatternResponse
import com.example.teumteum.data.remote.wish.WishRetrofitInterface
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.dto.RegisterWishResponse
import com.example.teumteum.ui.signup.view.NicknameJobFieldView
import com.example.teumteum.ui.signup.view.SleepPatternView
import com.example.teumteum.utils.getRetrofitWithToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class OnBoardingService {

    private lateinit var nicknameJobFieldView: NicknameJobFieldView
    private lateinit var sleepPatternView: SleepPatternView

    fun setNicknameJobFieldView(nicknameJobFieldView: NicknameJobFieldView) {
        this.nicknameJobFieldView = nicknameJobFieldView
    }

    fun setSleepPatternView(sleepPatternView: SleepPatternView){
        this.sleepPatternView = sleepPatternView
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

    //수면패턴 등록
    fun postSleepPattern(request: SleepPatternRequest) {
        val sleepPatternApi = getRetrofitWithToken().create(OnBoardingRetrofitInterface::class.java)
        val call = sleepPatternApi.postSleepPattern(request)
        Log.d("SLEEP_PATTERN_REQUEST", request.toString())


        call.enqueue(object : Callback<SleepPatternResponse> {
            override fun onResponse(
                call: Call<SleepPatternResponse>,
                response: Response<SleepPatternResponse>
            ) {
                if(response.isSuccessful) {
                    val body = response.body()
                    if(body != null && body.code == "ONBOARDING2005") {
                        sleepPatternView.onSleepPatternSuccess(body.code)
                    } else {
                        sleepPatternView.onSleepPatternFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    sleepPatternView.onSleepPatternFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<SleepPatternResponse>, t: Throwable) {
                sleepPatternView.onSleepPatternFailure("NETWORK_ERROR", t.localizedMessage)
            }

        })
    }
}