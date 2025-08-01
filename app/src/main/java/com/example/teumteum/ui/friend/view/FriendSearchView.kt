package com.example.teumteum.ui.friend.view

import com.example.teumteum.data.remote.friend.dto.FriendSearchResult

interface FriendSearchView {
    fun onSearchSuccess(result: List<FriendSearchResult>)
    fun onSearchFailure(code: String, message: String)
}
