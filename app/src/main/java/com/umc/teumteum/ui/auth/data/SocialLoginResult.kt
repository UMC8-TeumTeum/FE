package com.umc.teumteum.ui.auth.data

import com.umc.teumteum.utils.NextStep

data class SocialLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val nextStep: NextStep
)