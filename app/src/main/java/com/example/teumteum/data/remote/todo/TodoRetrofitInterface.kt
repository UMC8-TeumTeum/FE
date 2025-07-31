package com.example.teumteum.data.remote.todo

import com.example.teumteum.data.remote.todo.dto.EditTodoRequest
import com.example.teumteum.data.remote.todo.dto.EditTodoResponse
import com.example.teumteum.data.remote.todo.dto.GetTodoListResponse
import com.example.teumteum.data.remote.todo.dto.GetTodoResponse
import com.example.teumteum.data.remote.todo.dto.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.dto.RegisterTodoResponse
import com.example.teumteum.data.remote.wish.dto.EditWishRequest
import com.example.teumteum.data.remote.wish.dto.EditWishResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TodoRetrofitInterface {

    @POST("/api/home/todo")
    fun registerTodo(@Body request: RegisterTodoRequest): Call<RegisterTodoResponse>

    @GET("/api/home/todolist")
    fun getTodoList(@Query("date") date: String): Call<GetTodoListResponse>

    @GET("/api/home/todo/{todoId}")
    fun getTodo(@Path("todoId") todoId: Long): Call<GetTodoResponse>

    @PUT("/api/home/todo/{todoId}")
    fun editTodo(@Path("todoId") todoId: Long, @Body request: EditTodoRequest): Call<EditTodoResponse>
}