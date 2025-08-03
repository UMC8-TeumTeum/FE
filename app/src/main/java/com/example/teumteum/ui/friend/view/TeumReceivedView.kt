package com.example.teumteum.ui.friend.view

import com.example.teumteum.data.remote.friend.dto.TeumReceivedItem

interface TeumReceivedView {
    fun onTeumReceivedSuccess(teumList: List<TeumReceivedItem>)
    fun onTeumReceivedFailure(code: String, message: String)
}
