package com.example.teumteum.ui.signin.data

import com.example.teumteum.utils.NextStep

sealed class LoginResult {
    data class Success(val nextStep: NextStep) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object Loading : LoginResult()
}