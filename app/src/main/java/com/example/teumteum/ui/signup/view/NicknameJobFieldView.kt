package com.example.teumteum.ui.signup.view

interface NicknameJobFieldView {
    fun onNicknameJobSuccess(code: String)
    fun onNicknameJobFailure(code: String, message: String?)
}