package com.example.teumteum.ui.wish.view

import com.example.teumteum.data.entities.Wish

interface WishView {
    fun onGetWishSuccess(wish: Wish)
    fun onGetWishFailure(code: String, message: String? = null)
}