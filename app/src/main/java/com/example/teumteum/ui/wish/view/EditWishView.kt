package com.example.teumteum.ui.wish.view

interface EditWishView {
    fun onEditWishSuccess(code: String, message: String? = null)
    fun onEditWishFailure(code: String, message: String? = null)
}