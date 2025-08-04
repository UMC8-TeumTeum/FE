package com.example.teumteum.ui.signup.viewModel

sealed class OnBoardingUiState {
    object Loading : OnBoardingUiState()
    object Success : OnBoardingUiState()
    data class Error(val code: String, val message: String) : OnBoardingUiState()
    object Idle : OnBoardingUiState()
}