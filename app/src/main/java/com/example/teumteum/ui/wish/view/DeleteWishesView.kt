package com.example.teumteum.ui.wish.view

interface DeleteWishesView {
    fun onDeleteWishesSuccess(code: String, message: String? = null)
    fun onDeleteWishesFailure(code: String, message: String? = null)
}