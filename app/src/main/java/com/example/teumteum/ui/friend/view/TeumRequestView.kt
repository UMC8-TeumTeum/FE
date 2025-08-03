package com.example.teumteum.ui.friend.view


// 추후에 필요 - 틈 요청하기 api 연결 시
interface TeumRequestView {
    fun onTeumRequestSuccess(teumId: Int)
    fun onTeumRequestFailure(code: String, message: String)
}