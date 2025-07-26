package com.example.teumteum.ui.wish

import com.example.teumteum.data.entities.WishlistItem

interface WishRegisterView {
    fun onRegisterSuccess(code: String)
    fun onRegisterFailure(code: String)
}

interface WishlistView {
    fun onGetWishListSuccess(wishlist: List<WishlistItem>)
    fun onGetWishListFailure(code: String, message: String? = null)
}