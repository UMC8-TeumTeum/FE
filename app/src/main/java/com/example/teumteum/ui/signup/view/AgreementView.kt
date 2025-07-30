package com.example.teumteum.ui.signup.view

interface AgreementView {
    fun onAgreementSuccess(code: String)
    fun onAgreementFailure(code: String, message: String?)
}