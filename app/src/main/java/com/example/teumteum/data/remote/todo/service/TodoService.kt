package com.example.teumteum.data.remote.todo.service

import com.example.teumteum.data.remote.todo.model.DeleteTodoResponse
import com.example.teumteum.data.remote.todo.model.EditTodoRequest
import com.example.teumteum.data.remote.todo.model.EditTodoResponse
import com.example.teumteum.data.remote.todo.model.GetTodoListResponse
import com.example.teumteum.data.remote.todo.model.GetTodoResponse
import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.model.RegisterTodoResponse
import com.example.teumteum.data.remote.todo.model.TodoResult
import com.example.teumteum.data.remote.wish.model.RegisterWishResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TodoService {
    @POST("/api/home/todo")
    suspend fun registerTodo(@Body request: RegisterTodoRequest): Response<ApiResponse<TodoResult>>

    @GET("/api/home/todolist")
    suspend fun getTodoList(@Query("date") date: String): Call<GetTodoListResponse>

    @GET("/api/home/todo/{todoId}")
    suspend fun getTodo(@Path("todoId") todoId: Long): Call<GetTodoResponse>

    @PUT("/api/home/todo/{todoId}")
    suspend fun editTodo(@Path("todoId") todoId: Long, @Body request: EditTodoRequest): Call<EditTodoResponse>

    @DELETE("/api/home/todo/{todoId}")
    suspend fun deleteTodo(@Path("todoId") todoId: Long): Call<DeleteTodoResponse>

}