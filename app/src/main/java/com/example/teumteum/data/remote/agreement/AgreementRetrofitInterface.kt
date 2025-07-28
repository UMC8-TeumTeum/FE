package com.example.teumteum.data.remote.agreement

import com.example.teumteum.data.remote.agreement.dto.AgreementRequest
import com.example.teumteum.data.remote.agreement.dto.AgreementResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AgreementRetrofitInterface {
    @POST("/api/users/onboarding/agreement")
    fun postAgreements(@Body request: AgreementRequest): Call<AgreementResponse>

}