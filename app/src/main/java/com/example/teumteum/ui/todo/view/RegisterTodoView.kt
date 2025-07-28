package com.example.teumteum.ui.todo.view

interface RegisterTodoView {
    fun onRegisterTodoSuccess(code: String, todoId: Long?)
    fun onRegisterTodoFailure(code: String, message: String? = null)
}