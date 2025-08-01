package com.example.teumteum.ui.signup.view

import com.example.teumteum.data.remote.onboarding.dto.PresignedFileInfo

interface ProfileImageView {
    fun onPresignedSuccess(code: String, result: PresignedFileInfo)
    fun onPresignedFailure(code: String, message: String?)

    fun onProfileImageSuccess(code: String)
    fun onProfileImageFailure(code: String, message: String?)
}