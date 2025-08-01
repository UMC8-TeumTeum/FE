package com.example.teumteum.ui.todo.view

interface DeleteTodoView {
    fun onDeleteTodoSuccess(code: String, message: String? = null)
    fun onDeleteTodoFailure(code: String, message: String? = null)
}