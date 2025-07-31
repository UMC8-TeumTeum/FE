package com.example.teumteum.data.remote.todo

import com.example.teumteum.data.remote.todo.dto.GetTodoListResponse
import com.example.teumteum.data.remote.todo.dto.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.dto.RegisterTodoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TodoRetrofitInterface {
    @POST("/api/home/todo")
    fun registerTodo(@Body request: RegisterTodoRequest): Call<RegisterTodoResponse>
    @GET("/api/home/todolist")
    fun getTodoList(@Query("date") date: String): Call<GetTodoListResponse>
}