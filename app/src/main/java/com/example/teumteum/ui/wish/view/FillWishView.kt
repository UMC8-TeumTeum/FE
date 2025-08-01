package com.example.teumteum.ui.wish.view

interface FillWishView {
    fun onFillWishSuccess(code: String, message: String? = null)
    fun onFillWishFailure(code: String, message: String? = null)
}