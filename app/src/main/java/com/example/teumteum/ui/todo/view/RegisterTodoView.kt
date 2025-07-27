package com.example.teumteum.ui.todo.view

interface RegisterTodoView {
    fun onRegisterTodoSuccess(code: String, message: String? = null)
    fun onRegisterTodoFailure(code: String, message: String? = null)
}