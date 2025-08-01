package com.example.teumteum.ui.friend.view

import com.example.teumteum.data.remote.friend.dto.FriendProfileResult

interface FriendProfileView {
    fun onFriendProfileSuccess(result: FriendProfileResult)
    fun onFriendProfileFailure(code: String, message: String)
}