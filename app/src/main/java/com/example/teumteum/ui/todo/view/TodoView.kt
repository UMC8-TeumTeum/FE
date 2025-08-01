package com.example.teumteum.ui.todo.view

import com.example.teumteum.data.remote.todo.dto.GetTodoResult

interface TodoView {
    fun onGetTodoSuccess(todo: GetTodoResult)
    fun onGetTodoFailure(code: String, message: String? = null)
}