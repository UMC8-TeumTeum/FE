package com.example.teumteum.ui.todo.view

import com.example.teumteum.data.entities.TodoList

interface TodoListView {
    fun onGetTodoListSuccess(code: String, todoList: List<TodoList>)
    fun onGetTodoListFailure(code: String, message: String? = null)
}