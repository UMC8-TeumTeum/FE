package com.example.teumteum.utils

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T? = null
)