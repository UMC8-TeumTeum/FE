package com.example.teumteum.data.remote.onboarding

import com.example.teumteum.data.remote.onboarding.dto.NicknameJobRequest
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OnBoardingRetrofitInterface {
    @POST("/api/users/onboarding/nickname-job")
    fun postNicknameAndJobField(@Body request: NicknameJobRequest): Call<NicknameJobResponse>


}