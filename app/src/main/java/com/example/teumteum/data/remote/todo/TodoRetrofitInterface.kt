package com.example.teumteum.data.remote.todo

import com.example.teumteum.data.remote.todo.dto.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.dto.RegisterTodoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TodoRetrofitInterface {
    @POST("/api/home/todo")
    fun registerTodo(@Body request: RegisterTodoRequest): Call<RegisterTodoResponse>
}