package com.example.teumteum.ui.friend.view

interface TeumRequestView {
    fun onTeumRequestSuccess(teumId: Int)
    fun onTeumRequestFailure(code: String, message: String)
}