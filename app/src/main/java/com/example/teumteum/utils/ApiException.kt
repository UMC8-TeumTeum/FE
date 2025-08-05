package com.example.teumteum.utils

class ApiException(
    val code: String,
    override val message: String
) : Exception(message)