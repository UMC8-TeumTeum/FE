package com.example.teumteum.ui.wish.view

import androidx.lifecycle.ViewModel
import com.example.teumteum.data.remote.wish.dto.WishlistItem

class WishlistViewModel : ViewModel() {
    var wishlistItems: MutableList<WishlistItem> = mutableListOf()
}