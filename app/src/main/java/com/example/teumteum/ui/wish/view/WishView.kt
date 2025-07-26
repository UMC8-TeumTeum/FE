package com.example.teumteum.ui.wish

import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.entities.WishlistItem

interface RegisterWishView {
    fun onRegisterSuccess(code: String)
    fun onRegisterFailure(code: String)
}

interface WishlistView {
    fun onGetWishListSuccess(wishlist: List<WishlistItem>)
    fun onGetWishListFailure(code: String, message: String? = null)
}

interface WishView {
    fun onGetWishSuccess(wish: Wish)
    fun onGetWishFailure(code: String, message: String? = null)
}