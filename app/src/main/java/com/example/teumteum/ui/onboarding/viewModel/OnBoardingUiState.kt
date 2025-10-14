package com.example.teumteum.ui.onboarding.viewModel

sealed class OnBoardingUiState {
    object Idle : OnBoardingUiState()
    object Loading : OnBoardingUiState()
    object Success : OnBoardingUiState()
    data class PresignedSuccess(val presignedUrl: String, val fileName: String, val contentType: String) : OnBoardingUiState()
    data class Error(val code: String, val message: String) : OnBoardingUiState()
}