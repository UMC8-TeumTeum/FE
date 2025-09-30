package com.example.teumteum.data.remote.signup.repository

import android.util.Log
import com.example.teumteum.data.remote.signup.model.AgreementRequest
import com.example.teumteum.data.remote.signup.service.SignUpService
import com.example.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignUpRepository @Inject constructor(
    private val signUpService: SignUpService
) {

    // 약관동의
    suspend fun postAgreements(request: AgreementRequest): Result<Unit> = runCatching {
        val response = signUpService.postAgreements(request)
        Log.d("Agreements", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }
}