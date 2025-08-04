package com.example.teumteum.ui.signup.view

import com.example.teumteum.data.remote.onboarding.model.PresignedResponse

interface ProfileImageView {
    fun onPresignedSuccess(code: String, result: PresignedResponse)
    fun onPresignedFailure(code: String, message: String?)

    fun onProfileImageSuccess(code: String)
    fun onProfileImageFailure(code: String, message: String?)
}