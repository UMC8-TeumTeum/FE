package com.example.teumteum.ui.main.view

import com.example.teumteum.data.remote.home.dto.ScheduleResult

interface HomeView {
    fun onScheduleSuccess(code: String, result: List<ScheduleResult>)
    fun onScheduleFailure(code: String, message: String?)
}