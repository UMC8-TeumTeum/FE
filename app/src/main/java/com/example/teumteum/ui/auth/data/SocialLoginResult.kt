package com.example.teumteum.ui.auth.signin.data

import com.example.teumteum.utils.NextStep

data class SocialLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val nextStep: NextStep
)