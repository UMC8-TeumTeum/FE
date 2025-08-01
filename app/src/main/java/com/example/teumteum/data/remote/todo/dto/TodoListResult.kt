package com.example.teumteum.data.remote.todo.dto

import com.example.teumteum.data.entities.TodoList
import com.google.gson.annotations.SerializedName

data class TodoListResult(
    @SerializedName("todoList") val todoList: List<TodoList>
)