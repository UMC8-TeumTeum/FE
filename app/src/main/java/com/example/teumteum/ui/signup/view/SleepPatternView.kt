package com.example.teumteum.ui.signup.view

interface SleepPatternView {
    fun onSleepPatternSuccess(code: String)
    fun onSleepPatternFailure(code: String, message: String?)
}