package com.example.teumteum.data.remote.agreement

import com.example.teumteum.data.remote.onboarding.model.AgreementRequest
import com.example.teumteum.data.remote.agreement.dto.AgreementResponse
import com.example.teumteum.ui.signup.view.AgreementView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AgreementService @Inject constructor(
    private val agreementApi: AgreementRetrofitInterface
){
    private lateinit var agreementView: AgreementView

    fun setAgreementView(agreementView: AgreementView) {
        this.agreementView = agreementView
    }

    fun postAgreements(request: AgreementRequest) {

        agreementApi.postAgreements(request).enqueue(object : Callback<AgreementResponse> {
            override fun onResponse(
                call: Call<AgreementResponse>,
                response: Response<AgreementResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == "ONBOARDING2001") {
                        agreementView.onAgreementSuccess(body.code)
                    } else {
                        agreementView.onAgreementFailure(body?.code ?: "UNKNOWN", body?.message)
                    }
                } else {
                    agreementView.onAgreementFailure("HTTP_${response.code()}", response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<AgreementResponse>, t: Throwable) {
                agreementView.onAgreementFailure("NETWORK_ERROR", t.localizedMessage)
            }
        })
    }
}
