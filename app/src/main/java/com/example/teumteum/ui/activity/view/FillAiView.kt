package com.example.teumteum.ui.activity.view

interface FillAiView {
    fun onFillAiSuccess(code: String, message: String? = null)
    fun onFillAiFailure(code: String, message: String? = null)
}