package com.example.teumteum.ui.wish

interface WishView {
    fun onRegisterSuccess(code : String)
    fun onRegisterFailure(code : String)
}