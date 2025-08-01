package com.example.teumteum.ui.activity.view

import com.example.teumteum.data.remote.activity.dto.ActivityWishResult

interface ActivityWishView {
    fun onGetActivityWishSuccess(code: String, wishes: List<ActivityWishResult>)
    fun onGetActivityWishFailure(code: String, message: String? = null)
}