package com.umc.teumteum.ui.auth.data

import com.umc.teumteum.utils.NextStep

sealed class LoginResult {
    data class Success(val nextStep: NextStep) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object Loading : LoginResult()
}