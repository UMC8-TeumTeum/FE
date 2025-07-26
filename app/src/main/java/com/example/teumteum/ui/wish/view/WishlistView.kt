package com.example.teumteum.ui.wish.view

import com.example.teumteum.data.entities.WishlistItem

interface WishlistView {
    fun onGetWishListSuccess(wishlist:List<WishlistItem>)
    fun onGetWishListFailure(code: String, message: String? = null)
}