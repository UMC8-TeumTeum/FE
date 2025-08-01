package com.example.teumteum.ui.signup.view

interface ScheduleView {
    fun onScheduleSuccess(code: String)
    fun onScheduleFailure(code: String, message: String?)
}