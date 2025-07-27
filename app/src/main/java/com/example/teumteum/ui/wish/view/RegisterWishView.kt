package com.example.teumteum.ui.wish.view

interface RegisterWishView {
    fun onRegisterWishSuccess(code: String, message: String? = null)
    fun onRegisterWishFailure(code: String, message: String? = null)
}