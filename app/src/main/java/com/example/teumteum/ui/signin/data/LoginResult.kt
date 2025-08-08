package com.example.teumteum.ui.signin.data

sealed class LoginResult {
    data class Success(val jwt: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object Loading : LoginResult()
}