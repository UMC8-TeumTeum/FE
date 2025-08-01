package com.example.teumteum.ui.activity.view

import com.example.teumteum.data.remote.activity.dto.ActivityAiResult

interface ActivityAiView {
    fun onGetActivityAiSuccess(code: String, wishes: List<ActivityAiResult>)
    fun onGetActivityAiFailure(code: String, message: String? = null)
}