package com.example.teumteum.ui.todo.view

interface EditTodoView {
    fun onEditTodoSuccess(code: String, message: String? = null)
    fun onEditTodoFailure(code: String, message: String? = null)
}